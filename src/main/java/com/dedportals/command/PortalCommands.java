package com.dedportals.command;

import com.dedportals.DedPortalsMod;
import com.dedportals.portal.PortalAnimator;
import com.dedportals.portal.PortalBuilder;
import com.dedportals.portal.PortalData;
import com.dedportals.portal.PortalManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = DedPortalsMod.MODID)
public final class PortalCommands {
    private static final SuggestionProvider<CommandSourceStack> NAMES = (c, b) ->
            SharedSuggestionProvider.suggest(PortalManager.get(c.getSource().getServer()).names(), b);

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> name() {
        return Commands.argument("name", StringArgumentType.word()).suggests(NAMES);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> name(String arg) {
        return Commands.argument(arg, StringArgumentType.word()).suggests(NAMES);
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("dedportal").requires(s -> s.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> create(c, null))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(c -> create(c, BlockPosArgument.getLoadedBlockPos(c, "pos"))))))
                .then(Commands.literal("remove").then(name().executes(PortalCommands::remove)))
                .then(Commands.literal("enable").then(name().executes(c -> toggle(c, true))))
                .then(Commands.literal("disable").then(name().executes(c -> toggle(c, false))))
                .then(Commands.literal("setdest").then(name()
                        .then(Commands.literal("here").executes(PortalCommands::setDestHere))
                        .then(Commands.literal("to")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(PortalCommands::setDestTo))))))
                .then(Commands.literal("link").then(name("a").then(name("b")
                        .executes(c -> link(c, true))
                        .then(Commands.literal("oneway").executes(c -> link(c, false))))))
                .then(Commands.literal("unlink").then(name().executes(PortalCommands::unlink)))
                .then(Commands.literal("set").then(name()
                        .then(Commands.literal("effects").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(PortalCommands::setEffects)))
                        .then(Commands.literal("delay").then(Commands.argument("ticks", IntegerArgumentType.integer(-1, 200))
                                .executes(PortalCommands::setDelay)))))
                .then(Commands.literal("info").then(name().executes(PortalCommands::info)))
                .then(Commands.literal("list").executes(PortalCommands::list))
                .then(Commands.literal("tp").then(name().executes(PortalCommands::tp))));
    }

    // ---------------------------------------------------------------- helpers
    private static PortalManager mgr(CommandContext<CommandSourceStack> c) {
        return PortalManager.get(c.getSource().getServer());
    }

    private static CommandSyntaxException err(String msg) {
        return new SimpleCommandExceptionType(Component.literal(msg)).create();
    }

    private static PortalData need(CommandContext<CommandSourceStack> c, String arg) throws CommandSyntaxException {
        String n = StringArgumentType.getString(c, arg).toLowerCase(Locale.ROOT);
        PortalData d = mgr(c).get(n);
        if (d == null) throw err("El portal '" + n + "' no existe.");
        return d;
    }

    private static ServerLevel levelOf(CommandContext<CommandSourceStack> c, PortalData d) throws CommandSyntaxException {
        ServerLevel l = c.getSource().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, d.dimension));
        if (l == null) throw err("La dimension del portal no esta disponible: " + d.dimension);
        return l;
    }

    private static void ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), true);
    }

    private static String fmt(Vec3 v) {
        return String.format(Locale.ROOT, "%.1f %.1f %.1f", v.x, v.y, v.z);
    }

    // ---------------------------------------------------------------- commands
    private static int create(CommandContext<CommandSourceStack> c, BlockPos at) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerLevel level = c.getSource().getLevel();
        String n = StringArgumentType.getString(c, "name").toLowerCase(Locale.ROOT);
        PortalManager m = mgr(c);
        if (m.get(n) != null) throw err("Ya existe un portal llamado '" + n + "'.");
        Direction look = p.getDirection();
        Direction.Axis axis = look.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        BlockPos origin = at != null ? at : p.blockPosition().relative(look, 3);
        PortalData d = new PortalData(n, level.dimension().location(), origin, axis, look.getOpposite());
        m.add(d);
        PortalBuilder.buildFrame(level, d);
        PortalBuilder.setInterior(level, d, false);
        PortalAnimator.start(level, d, true);
        ok(c, "Portal '" + n + "' creado. Usa /dedportal setdest o /dedportal link para darle destino.");
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        PortalAnimator.cancel(d);
        PortalBuilder.removeAll(levelOf(c, d), d);
        mgr(c).remove(d.name);
        ok(c, "Portal '" + d.name + "' eliminado.");
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> c, boolean on) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        d.enabled = on;
        mgr(c).setDirty();
        ServerLevel level = levelOf(c, d);
        if (on) PortalBuilder.buildFrame(level, d); // reconstruye marco si faltara, interior a aire
        PortalBuilder.setInterior(level, d, false);
        PortalAnimator.start(level, d, on);
        ok(c, "Portal '" + d.name + "' " + (on ? "activado" : "desactivado") + ".");
        return 1;
    }

    private static int setDestHere(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        ServerPlayer p = c.getSource().getPlayerOrException();
        d.setDestination(p.level().dimension().location(), p.getX(), p.getY(), p.getZ(), p.getYRot());
        mgr(c).setDirty();
        ok(c, "Destino de '" + d.name + "': " + d.destDimension + " @ " + fmt(p.position()));
        return 1;
    }

    private static int setDestTo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        ServerLevel dim = DimensionArgument.getDimension(c, "dimension");
        Vec3 v = Vec3Argument.getVec3(c, "pos");
        d.setDestination(dim.dimension().location(), v.x, v.y, v.z, 0.0F);
        mgr(c).setDirty();
        ok(c, "Destino de '" + d.name + "': " + d.destDimension + " @ " + fmt(v));
        return 1;
    }

    private static int link(CommandContext<CommandSourceStack> c, boolean both) throws CommandSyntaxException {
        PortalData a = need(c, "a");
        PortalData b = need(c, "b");
        if (a == b) throw err("No se puede vincular un portal consigo mismo.");
        Vec3 eb = b.exitPos();
        a.setDestination(b.dimension, eb.x, eb.y, eb.z, b.facing.toYRot());
        if (both) {
            Vec3 ea = a.exitPos();
            b.setDestination(a.dimension, ea.x, ea.y, ea.z, a.facing.toYRot());
        }
        mgr(c).setDirty();
        ok(c, "'" + a.name + "' -> '" + b.name + "'" + (both ? " (bidireccional)" : " (un sentido)"));
        return 1;
    }

    private static int unlink(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        d.hasDest = false;
        mgr(c).setDirty();
        ok(c, "Portal '" + d.name + "' sin destino.");
        return 1;
    }

    private static int setEffects(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        d.effects = BoolArgumentType.getBool(c, "value");
        mgr(c).setDirty();
        ok(c, "Efecto visual de '" + d.name + "': " + d.effects);
        return 1;
    }

    private static int setDelay(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        d.delay = IntegerArgumentType.getInteger(c, "ticks");
        mgr(c).setDirty();
        ok(c, "Retardo de '" + d.name + "': " + (d.delay < 0 ? "config por defecto" : d.delay + " ticks"));
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        StringBuilder sb = new StringBuilder();
        sb.append("Portal '").append(d.name).append("' [").append(d.enabled ? "activo" : "inactivo").append("]\n");
        sb.append(" Posicion: ").append(d.dimension).append(" @ ").append(d.origin.toShortString())
                .append(" eje ").append(d.axis.getName()).append(", frente ").append(d.facing.getName()).append('\n');
        sb.append(" Destino: ").append(d.hasDest
                ? d.destDimension + " @ " + fmt(new Vec3(d.dx, d.dy, d.dz)) : "(ninguno)").append('\n');
        sb.append(" Efectos: ").append(d.effects).append(", retardo: ").append(d.delay < 0 ? "defecto" : d.delay);
        String s = sb.toString();
        c.getSource().sendSuccess(() -> Component.literal(s), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        PortalManager m = mgr(c);
        if (m.all().isEmpty()) {
            c.getSource().sendSuccess(() -> Component.literal("No hay portales."), false);
            return 0;
        }
        for (PortalData d : m.all()) {
            String s = "- " + d.name + (d.enabled ? "" : " (inactivo)") + " @ " + d.dimension + " " + d.origin.toShortString()
                    + " -> " + (d.hasDest ? d.destDimension + " " + fmt(new Vec3(d.dx, d.dy, d.dz)) : "sin destino");
            c.getSource().sendSuccess(() -> Component.literal(s), false);
        }
        return m.all().size();
    }

    private static int tp(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        PortalData d = need(c, "name");
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerLevel level = levelOf(c, d);
        Vec3 v = d.exitPos();
        p.teleportTo(level, v.x, v.y, v.z, d.facing.getOpposite().toYRot(), 0.0F);
        ok(c, "Teletransportado al portal '" + d.name + "'.");
        return 1;
    }

    private PortalCommands() {}
}
