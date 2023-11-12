package com.example.iso8583.iso8583;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import jakarta.annotation.PostConstruct;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.ISO93BPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.iso8583.util.Logger;
import com.example.iso8583.exception.ConflictException;

@Service
public class ISO8583Client {
  private static final Logger logger = new Logger(ISO8583Client.class);
  private final ISO93BPackager isoPacker;
  private final ISO8583MessageHandler isoMsgHandler;
  private final Map<String, ISO8583MessageQueue> outgoingIsoMessagePromises = new HashMap<>();
  @Value("${iso8583.host}")
  private String host;
  @Value("${iso8583.port}")
  private int port;
  @Value("${iso8583.issuerInstitutionId}")
  private String issuerInstitutionId;
  @Value("${iso8583.acquirerInstitutionId}")
  private String acquirerInstitutionId;
  private Channel channel;
  private ScheduledExecutorService isoEchoMsgExecutor;


  public ISO8583Client(ISO8583Packer isoPacker, ISO8583MessageHandler isoMsgHandler) {
    this.isoPacker = isoPacker;
    this.isoMsgHandler = isoMsgHandler;
  }

  @PostConstruct
  public void connect() {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .remoteAddress(new InetSocketAddress(host, port))
             .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel ch) {
                 channel = ch;
                 ch.pipeline().addLast(createInboundChannelHandler());
               }
             });

    ChannelFuture future = bootstrap.connect();
    future.addListener(createChannelFutureListener(group, future));
  }

  public void reconnect(EventLoopGroup group) {
    logger.info("Reconnecting in %d seconds...%n".formatted(5));
    group.shutdownGracefully();
    try {
      isoEchoMsgExecutor.shutdown();
    } catch (Exception ignore) {}
    ISOUtil.sleep(TimeUnit.SECONDS.toMillis(5));
    connect();
  }


  public ChannelInboundHandlerAdapter createInboundChannelHandler() {
    return new ChannelInboundHandlerAdapter() {
      @Override
      public void channelActive(ChannelHandlerContext ctx) throws ISOException {
        logger.info("Connected to the server.");
        sendMessage(ISO8583Helper.buildSignOnMessage(isoPacker, issuerInstitutionId, acquirerInstitutionId), "Outgoing iso8583 sign-on message:");
        isoEchoMsgExecutor = Executors.newScheduledThreadPool(1);
        isoEchoMsgExecutor.scheduleAtFixedRate(() -> {
          try {
            sendMessage(ISO8583Helper.buildEchoMessage(isoPacker, issuerInstitutionId, acquirerInstitutionId), "Outgoing iso8583 periodic echo message:");
          } catch (ISOException e) {
            logger.error("Failed packing echo message: " + e.getMessage());
          }
        }, 0, 10, TimeUnit.SECONDS);
      }

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ISOMsg isoMsg = new ISOMsg();
        ByteBuf buf = (ByteBuf) msg;
        byte[] isoMsgBytes = new byte[buf.readableBytes()];
        buf.readBytes(isoMsgBytes);
        buf.release();
        try {
          isoMsg.setPackager(isoPacker);
          isoMsg.unpack(isoMsgBytes);
          logger.info("Incoming iso8583 message:");
          ISO8583Helper.logMessage(isoPacker, isoMsg, isoMsgBytes);
          checkMessageResponse(isoMsgBytes, isoMsg);
          handleMessage(isoMsg);
        } catch (ISOException e) {
          logger.error("Failed to unpack message: %s".formatted(e.getMessage()));
          checkMessageResponse(isoMsgBytes, isoMsg);
          logger.error(new String(isoMsgBytes, StandardCharsets.US_ASCII));
          logger.error(ISOUtil.byte2hex(isoMsgBytes));
          logger.error(ISOUtil.hexdump(isoMsgBytes));
          isoMsg.dump(System.err, "");
        }
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("Channel error: %s".formatted(e.getMessage()));
        ctx.close();
      }
    };
  }

  public ChannelFutureListener createChannelFutureListener(EventLoopGroup group, ChannelFuture future) {
    return new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture channelFuture) {
        if (channelFuture.isSuccess()) {
          channel = channelFuture.channel();
          channel.closeFuture().addListener(createChannelCloseFutureListener(group));
          return;
        }
        logger.error("Connection to the server failed.");
        future.removeListener(this);
        channelFuture.removeListener(this);
        channelFuture.channel().eventLoop().shutdownGracefully();
        reconnect(group);
      }
    };
  }

  public ChannelFutureListener createChannelCloseFutureListener(EventLoopGroup group) {
    return new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture closeFuture) {
        logger.error("Connection to the server closed.");
        closeFuture.removeListener(this);
        closeFuture.channel().eventLoop().shutdownGracefully();
        reconnect(group);
      }
    };
  }

  public Promise<byte[]> sendMessage(ISOMsg isoMsg, String narration) throws ISOException {
    byte[] isoMsgBytes = isoMsg.pack();
    logger.info(narration);
    ISO8583Helper.logMessage(isoPacker, isoMsg, isoMsgBytes);

    if (channel == null || !channel.isWritable()) {
      logger.error("Channel is not writable.");
      return null;
    }

    ByteBuf buf = channel.alloc().buffer(isoMsgBytes.length);
    buf.writeBytes(isoMsgBytes);
    channel.writeAndFlush(buf);
    return queueSentMessage(isoMsg);
  }

  public void sendMessage(ISOMsg isoMsg) throws ISOException {
    sendMessage(isoMsg, "Outgoing iso8583 message:");
  }

  private Promise<byte[]> queueSentMessage(ISOMsg isoMsg) {
    String stan = isoMsg.getValue(11).toString();
    EventExecutor executor = new DefaultEventLoop();
    Promise<byte[]> outgoingIsoMessagePromise = new DefaultPromise<>(executor);
    ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);

    if (outgoingIsoMessagePromises.get(stan) != null) {
      timeoutExecutor.close();
      outgoingIsoMessagePromise.setFailure(new ConflictException("Message already queued"));
      return outgoingIsoMessagePromise;
    }

    outgoingIsoMessagePromises.put(stan, new ISO8583MessageQueue(isoMsg, outgoingIsoMessagePromise));
    timeoutExecutor.schedule(() -> {
      if (!outgoingIsoMessagePromise.isDone()) {
        outgoingIsoMessagePromise.setFailure(new TimeoutException("Operation timed out"));
      }
      outgoingIsoMessagePromises.remove(stan);
      timeoutExecutor.shutdown();
    }, 40, TimeUnit.SECONDS);

    return outgoingIsoMessagePromise;
  }

  private void checkMessageResponse(byte[] isoMsgBytes, ISOMsg isoMsg) {
    var outgoingIsoMessagePromise = outgoingIsoMessagePromises.get(isoMsg.getValue(11).toString());
    if (outgoingIsoMessagePromise == null ||
        Objects.equals(isoMsg.getValue(0).toString(), outgoingIsoMessagePromise.isoMsg().getValue(0).toString())) {
      return;
    }
    outgoingIsoMessagePromise.isoMessagePromise().setSuccess(isoMsgBytes);
    outgoingIsoMessagePromises.remove(isoMsg.getValue(11).toString());
  }

  public void handleMessage(ISOMsg isoMsg) throws ISOException {
    ISOMsg response;
    switch (isoMsg.getMTI()) {
      case ISO8583MessageType.NETWORK_MANAGEMENT_REQUEST -> response = isoMsgHandler.handleNetworkMessages(isoMsg);
      case ISO8583MessageType.FINANCIAL_TRANSACTION_REQUEST -> response = isoMsgHandler.handleFinancialMessages(isoMsg);
      default -> {
        String stan = isoMsg.getValue(11) != null ? isoMsg.getValue(11).toString() : "";
        logger.error("Unhandled iso8583 message - type: %s - stan: %s".formatted(isoMsg.getMTI(), stan));
        return;
      }
    }
    if (response != null) {
      sendMessage(response);
    }
  }
}


record ISO8583MessageQueue(ISOMsg isoMsg, Promise<byte[]> isoMessagePromise) {}
