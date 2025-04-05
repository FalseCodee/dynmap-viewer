package me.falsecode.dynmapnametags;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2i;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NametagModule {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final ArrayList<NametagEntry> dynmapEntries = new ArrayList<>();
    private final Timer dynmapUpdateTimer = new Timer();
    private static final String url = "http://map.gunclash.net:8123/tiles/players.json";
    private final HashSet<Vector2i> textMap = new HashSet<>();
    private final HashMap<Vector2i, Vec2f> textToScreenPos = new HashMap<>();
    public void onTick() {
        if(!dynmapUpdateTimer.hasTimeElapsed(10000, true))  return;

        FalseRunnable.of(() -> {
            try {
                JsonObject json = fromUrl(url);
                JsonArray players = json.getAsJsonArray("players");
                ArrayList<NametagEntry> toAdd = new ArrayList<>();
                String dimension = players.asList().stream()
                        .filter(jsonElement -> jsonElement.getAsJsonObject().get("uuid").getAsString().equals(mc.player.getUuid().toString().replace("-", "")))
                        .map(jsonElement -> jsonElement.getAsJsonObject().get("world").getAsString())
                        .findFirst().orElse("minecraft_overworld");

                for(JsonElement ele : players.asList().stream()
                        .filter(jsonElement -> jsonElement.getAsJsonObject().get("world").getAsString().equals(dimension)).toList()) {
                    JsonObject player = ele.getAsJsonObject();
                    String name = player.get("name").getAsString();
                    UUID uuid = UUID.fromString(
                            addHyphensToUuid(player.get("uuid").getAsString()));
                    if(uuid.equals(mc.player.getUuid())) continue;

                    int x = player.get("x").getAsInt();
                    int y = mc.player.getBlockY();
                    int z = player.get("z").getAsInt();

                    int health = player.get("health").getAsInt();


                    toAdd.add(new NametagEntry(name, uuid, new Vec3d(x, y, z), health));
                }

                mc.submitAndJoin(() -> {
                    dynmapEntries.clear();
                    dynmapEntries.addAll(toAdd);
                });
            } catch (RuntimeException ignored) {}
        }).runTaskAsync();
    }

    public void onRender(DrawContext context, float tickDelta) {
        List<Entity> entityList = new ArrayList<>();
        mc.world.getEntities().forEach(entityList::add);
        entityList = entityList.stream().filter(entity -> entity instanceof LivingEntity && entity != mc.player).collect(Collectors.toList());

        List<Entity> finalEntityList = entityList;
        dynmapEntries.stream().filter(nametagEntry -> finalEntityList.stream().noneMatch(entity -> entity.getUuid().equals(nametagEntry.uuid()))).forEach(nametagEntry ->
                renderLabel(nametagEntry.name(), nametagEntry.uuid(), nametagEntry.pos(), nametagEntry.health(), 20, context, false, true, false));
        textMap.clear();
        textToScreenPos.clear();
    }

    private void renderLabel(String name, UUID uuid, Vec3d location, int health, int maxHealth, DrawContext context, boolean drawHealth, boolean drawDistance, boolean drawPing) {
        TextRenderer fr = mc.textRenderer;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        Vec2f pos = ProjectionUtils.toScreenXY(location);
        float textHeight = (float) mc.getWindow().getHeight() / fr.fontHeight;
        Vector2i scaledPos = new Vector2i((int) (Math.floor(pos.x / textHeight)),
                (int) Math.floor(pos.y / fr.fontHeight));
        boolean changedHeight = false;
        int i = 0;
        while(textMap.contains(scaledPos) && i < 20) {
            pos = pos.add(new Vec2f(0, -fr.fontHeight));
            pos = new Vec2f(textToScreenPos.get(scaledPos).x, pos.y);
            scaledPos = new Vector2i((int) Math.floor(pos.x / textHeight),
                    (int) Math.floor(pos.y / fr.fontHeight));
            changedHeight = true;
            i++;
        }
        textMap.add(scaledPos);
        textToScreenPos.put(scaledPos, pos);
        matrices.translate(pos.x, pos.y, 0);
        matrices.scale(0.5f, 0.5f, 1);
        String displayName = " " + name + " (" + Math.round(mc.player.getPos().distanceTo(location)) + "m)";

        context.drawCenteredTextWithShadow(fr, displayName, 6, -12, -1);



        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        PlayerListEntry playerListEntry = null;

        if(networkHandler != null) playerListEntry = networkHandler.getPlayerListEntry(uuid);
        if(playerListEntry != null) {
            int x = (int) (-fr.getWidth(displayName)/2f-12)+6;
            int y = -12;

            PlayerSkinDrawer.draw(context, playerListEntry.getSkinTextures(), x, y, 12);
        }
        matrices.pop();
    }

    public static String addHyphensToUuid(String uuidWithoutHyphens) {
        return uuidWithoutHyphens.replaceFirst (
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

    public static JsonObject fromUrl(String urlString) throws RuntimeException {
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();

            return JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private record NametagEntry(String name, UUID uuid, Vec3d pos, int health) { }
}
