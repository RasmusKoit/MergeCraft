package eu.ialbhost.mergecraft.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class PacketListener {
    public PacketListener(MergeCraft mergeCraft) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(
                new PacketAdapter(mergeCraft, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();
                        User user = MergeCraft.getInstance().matchUser(player);
                        PacketContainer packet = event.getPacket();
                        StructureModifier<Integer> ints = packet.getIntegers();
                        StructureModifier<byte[]> byteArray = packet.getByteArrays();
                        int cx = ints.read(0);
                        int cz = ints.read(1);
                        World world = player.getWorld();
                        Chunk chunk = world.getChunkAt(cx, cz);
                        if (!user.shouldRender(chunk)) {
                            byteArray.write(0, new byte[byteArray.read(0).length]);
                            StructureModifier<List<NbtBase<?>>> list = packet.getListNbtModifier();
                            list.write(0, new ArrayList<>());
                            event.setPacket(packet);
                        }

                    }
                }
        );
    }
}
