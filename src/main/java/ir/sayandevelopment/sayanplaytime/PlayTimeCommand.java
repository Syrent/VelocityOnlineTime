package ir.sayandevelopment.sayanplaytime;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ir.sayandevelopment.sayanplaytime.database.SQL;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayTimeCommand implements SimpleCommand {

    public static final String PREFIX = "<gradient:#F2E205:#F2A30F>PlayTime</gradient> <color:#555197>| ";
    @Override
    public void execute(Invocation invocation) {
        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();
        MiniMessage formatter = MiniMessage.get();

        if (args.length == 0) {
            SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                try {
                    long total_time = SayanPlayTime.SQL.getPlayerPlayTime(player.getUniqueId(), "total_time");

                    long seconds = total_time / 1000;
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(formatter.deserialize(String.format(
                            PREFIX + "<color:#00F3FF>Total playtime:</color> <color:#C0D3EF>%sh %sm", hours, minutes)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).schedule();
        } else {
            if (args[0].equalsIgnoreCase("get")) {
                if (args.length == 2) {
                    String userName = args[1];
                    SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                        try {
                            long total_time = SayanPlayTime.SQL.getPlayerPlayTime(userName, "total_time");

                            if (total_time == 0) {
                                player.sendMessage(formatter.deserialize(
                                        PREFIX + "<color:#D72D32>Player not found!"));
                                return;
                            }

                            long seconds = total_time / 1000;
                            int hours = (int) (seconds / 3600);
                            int minutes = (int) ((seconds % 3600) / 60);

                            player.sendMessage(formatter.deserialize(String.format(
                                    PREFIX + "<color:#C1D6F1>%s<color:#00F3FF>'s Total playtime:</color> <color:#C0D3EF>%sh %sm", userName, hours, minutes)));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).schedule();
                }
                if (args.length == 3) {
                    String userName = args[1];
                    SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                        try {
                            long total_time = SayanPlayTime.SQL.getPlayerPlayTime(userName, args[2].toLowerCase());

                            if (total_time == 0) {
                                player.sendMessage(formatter.deserialize(String.format(
                                        PREFIX + "<color:#D72D32>Player playtime is empty on <color:#C1D6F1>%s</color>!", Utils.capitalize(args[2])
                                )));
                                return;
                            }

                            long seconds = total_time / 1000;
                            int hours = (int) (seconds / 3600);
                            int minutes = (int) ((seconds % 3600) / 60);

                            player.sendMessage(formatter.deserialize(String.format(
                                    PREFIX + "<color:#C1D6F1>%s</color><color:#00F3FF>'s playtime in <color:#C1D6F1>%s</color>:</color> <color:#C0D3EF>%sh %sm",
                                    userName, Utils.capitalize(args[2]), hours, minutes)));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).schedule();
                }
            } else if (args[0].equalsIgnoreCase("top")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("week") || args[1].equalsIgnoreCase("weekly")) {

                        try {
                            List<PPlayer> pPlayers = SayanPlayTime.SQL.getWeeklyTops(5);

                            player.sendMessage(formatter.deserialize("<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                    " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                                    "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"));

                            for (int i = 0; i < 5; i++) {
                                long seconds = pPlayers.get(i).getTime() / 1000;
                                int hours = (int) (seconds / 3600);
                                int minutes = (int) ((seconds % 3600) / 60);

                                player.sendMessage(formatter.deserialize(String.format(
                                        "<color:#EE9900>[<color:#F9BD03>%s<color:#EE9900>] <color:#C1D6F1>%s</color><color:#00F3FF> | </color> <color:#C0D3EF>%sh %sm",
                                        i + 1, pPlayers.get(i).getUserName(), hours, minutes)));
                            }
                        } catch (Exception ignored) {
                        }
                        return;
                    }
                }
                SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                    try {
                        List<PPlayer> pPlayers = SayanPlayTime.SQL.getTopPlayTimes(5);

                        player.sendMessage(formatter.deserialize("<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                                "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"));

                        for (int i = 0; i < 5; i++) {
                            long seconds = pPlayers.get(i).getTime() / 1000;
                            int hours = (int) (seconds / 3600);
                            int minutes = (int) ((seconds % 3600) / 60);

                            player.sendMessage(formatter.deserialize(String.format(
                                    "<color:#EE9900>[<color:#F9BD03>%s<color:#EE9900>] <color:#C1D6F1>%s</color><color:#00F3FF> | </color> <color:#C0D3EF>%sh %sm",
                                    i + 1, pPlayers.get(i).getUserName(), hours, minutes)));
                        }
                    } catch (Exception ignored) {
                    }
                }).schedule();
            } else if (args[0].equalsIgnoreCase("weekly")) {
                SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                    try {
                        player.sendMessage(formatter.deserialize("<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                            " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                            "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"));

                        for (int i = 0; i < 5; i++) {
                            long seconds = SayanPlayTime.SQL.getWeeklyPlayTime(player.getUniqueId()) / 1000;
                            int hours = (int) (seconds / 3600);
                            int minutes = (int) ((seconds % 3600) / 60);

                            player.sendMessage(formatter.deserialize(String.format(PREFIX +
                                            "<color:#C1D6F1>%s<color:#00F3FF>'s Total playtime:</color> <color:#C0D3EF>%sh %sm",
                                    player.getUsername(), hours, minutes)));
                        }
                    } catch (Exception ignored) {}
                }).schedule();
            } else if (args[0].equalsIgnoreCase("debug")) {
                /*if (!player.hasPermission("sayanplaytime.admin"))
                   return;*/

                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("discord")) {
                        try {
                            DiscordManager.getInstance().sendDailyMessage();

                            try {
                                SayanPlayTime.SQL.resetDaily();
                                SayanPlayTime.SQL.resetDaily();
                                SayanPlayTime.SQL.resetDaily();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception ignored) {
                        }
                        //DiscordManager.getInstance().sendWinnerMessage();
                    }
                }

            } else if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(formatter.deserialize("<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                        " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                        "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime weekly"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime <color:#00F3FF><gamemode>"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime get <color:#00F3FF><user>"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime get <color:#00F3FF><user> <gamemode>"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime top"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime top weekly"));
                player.sendMessage(formatter.deserialize("<color:#F2E205>/playtime help"));
            } else {
                SayanPlayTime.INSTANCE.getServer().getScheduler().buildTask(SayanPlayTime.INSTANCE, () -> {
                    try {
                        long total_time = SayanPlayTime.SQL.getPlayerPlayTime(player.getUniqueId(), args[0].toLowerCase());

                        if (total_time == 0) {
                            player.sendMessage(formatter.deserialize(String.format(
                                    PREFIX + "<color:#D72D32>You don't have any data in <color:#C1D6F1>%s</color>!", Utils.capitalize(args[0]))));
                            return;
                        }

                        long seconds = total_time / 1000;
                        int hours = (int) (seconds / 3600);
                        int minutes = (int) ((seconds % 3600) / 60);

                        player.sendMessage(formatter.deserialize(String.format(
                                PREFIX + "<color:#C1D6F1>%s</color><color:#00F3FF> playtime:</color> <color:#C0D3EF>%sh %sm",
                                Utils.capitalize(args[0]), hours, minutes)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).schedule();
            }
        }
    }
    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> list = new ArrayList<>();
        String[] args = invocation.arguments();
        SayanPlayTime.INSTANCE.getLogger().warn("Arg Length: " + args.length);

        if (args.length <= 1) {
            list.add("help");
            list.add("weekly");
            list.add("get");
            list.add("top");
            for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
                list.add(server.getServerInfo().getName());
            }
        } else {
            if (args[0].equalsIgnoreCase("get")) {
                if (args.length >= 3) {
                    for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
                        if (args[2].isEmpty()) {
                            list.add(server.getServerInfo().getName());
                        } else {
                            if (server.getServerInfo().getName().toLowerCase().toLowerCase().startsWith(args[2])) {
                                list.add(server.getServerInfo().getName());
                            }
                        }
                    }
                } else {
                    for (Player player : SayanPlayTime.INSTANCE.getServer().getAllPlayers()) {
                        if (args[1].isEmpty()) {
                            list.add(player.getUsername());
                        } else {
                            if (player.getUsername().toLowerCase().toLowerCase().startsWith(args[1])) {
                                list.add(player.getUsername());
                            }
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("top")) {
                list.add("weekly");
            }
        }

        return list;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CompletableFuture<List<String>> listCompletableFuture = new CompletableFuture<>();
        List<String> list = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            list.add("help");
            list.add("weekly");
            list.add("get");
            list.add("top");
            for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
                list.add(server.getServerInfo().getName());
            }
        } else {
            if (args[0].equalsIgnoreCase("get")) {
                if (args.length >= 3) {
                    for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
                        if (args[2].isEmpty()) {
                            list.add(server.getServerInfo().getName());
                        } else {
                            if (server.getServerInfo().getName().toLowerCase().toLowerCase().startsWith(args[2])) {
                                list.add(server.getServerInfo().getName());
                            }
                        }
                    }
                } else {
                    for (Player player : SayanPlayTime.INSTANCE.getServer().getAllPlayers()) {
                        if (args[1].isEmpty()) {
                            list.add(player.getUsername());
                        } else {
                            if (player.getUsername().toLowerCase().toLowerCase().startsWith(args[1])) {
                                list.add(player.getUsername());
                            }
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("top")) {
                list.add("weekly");
            }
        }


        listCompletableFuture.complete(list);
        return listCompletableFuture;
    }

}
