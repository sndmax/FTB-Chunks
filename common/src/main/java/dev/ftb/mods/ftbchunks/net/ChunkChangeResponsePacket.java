package dev.ftb.mods.ftbchunks.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbchunks.client.ChunkScreen;
import dev.ftb.mods.ftbchunks.data.ClaimResults;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;

public class ChunkChangeResponsePacket extends BaseS2CMessage {
    private final int totalChunks;
    private final int changedChunks;
    private final EnumMap<ClaimResults,Integer> problems;

    public ChunkChangeResponsePacket(int totalChunks, int changedChunks, EnumMap<ClaimResults,Integer> problems) {
        this.totalChunks = totalChunks;
        this.changedChunks = changedChunks;
        this.problems = problems;
    }

    ChunkChangeResponsePacket(FriendlyByteBuf buf) {
        totalChunks = buf.readVarInt();
        changedChunks = buf.readVarInt();
        problems = new EnumMap<>(ClaimResults.class);

        int nProblems = buf.readVarInt();
        for (int i = 0; i < nProblems; i++) {
            String name = buf.readUtf(Short.MAX_VALUE);
            int count = buf.readVarInt();
            ClaimResults.forName(name).ifPresent(res -> problems.put(res, count));
        }
    }

    @Override
    public MessageType getType() {
        return FTBChunksNet.CHUNK_CHANGE_RESPONSE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(totalChunks);
        buf.writeVarInt(changedChunks);
        buf.writeVarInt(problems.size());
        problems.forEach((res, count) -> {
            buf.writeUtf(res.claimResultName());
            buf.writeVarInt(count);
        });
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ChunkScreen.notifyChunkUpdates(totalChunks, changedChunks, problems);
    }
}
