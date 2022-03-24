package com.ducmami.quarkustinode;

import com.google.protobuf.ByteString;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestQuery;
import pbx.MutinyNodeGrpc;
import pbx.Tinode;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Path("/public/underlord")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TinodeResource {

    @GrpcClient("tinode")
    MutinyNodeGrpc.MutinyNodeStub nodeStub;

    @GET
    @Path("/hello-grpc")
    public Multi<String> helloGrpc(@RestQuery String name) throws UnsupportedEncodingException {
        String encodedString = Base64.getEncoder().encodeToString("ducmami:123456".getBytes());
        Tinode.ClientLogin clientLogin = Tinode.ClientLogin.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setScheme("basic")
                .setSecret(ByteString.copyFrom(encodedString, "UTF-8"))
                .build();
        Tinode.ClientMsg msg = Tinode.ClientMsg.newBuilder()
                .setLogin(clientLogin)
                .build();

        final ByteString nullByteString  = ByteString.copyFrom("null", "UTF-8");
        Multi<Tinode.ServerMsg> serverMsgMulti = nodeStub.messageLoop(Multi.createFrom().item(msg));
        return serverMsgMulti
                .onItem()
                .transform(serverMsg -> {
                    return serverMsg.getCtrl().getParamsOrDefault("user",nullByteString).toStringUtf8()+":" + serverMsg.getCtrl().getParamsOrDefault("user",nullByteString).toStringUtf8();
                });
    }
}
