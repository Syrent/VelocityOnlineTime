package ir.sayandevelopment.sayanplaytime;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;

public class DiscordManager extends ListenerAdapter {

    private static DiscordManager instance;
    public static DiscordManager getInstance() {
        return instance;
    }

    private final TextChannel playtimeChannel = SayanPlayTime.JDA.getTextChannelById(932707742391607316L);
    private final TextChannel staffPlayTimeChannel = SayanPlayTime.JDA.getTextChannelById(913493000825479189L);

    public DiscordManager() {
        instance = this;
    }

    public void sendWinnerMessage() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(String.format("\uD83E\uDDED  PlayTime | %s", DateUtils.getCurrentShamsidate()), null);

        embed.setColor(new Color(0xc1d6f1));

        try {
            List<PPlayer> pPlayerList = SayanPlayTime.SQL.getWeeklyTops(3);
            long total_time = pPlayerList.get(0).getTime();
            long seconds = total_time / 1000;
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);

            embed.appendDescription("⏱️ مسابقه بیشترین پلی تایم این هفته سرور به پایان رسید!");
            embed.appendDescription("\n");
            embed.appendDescription("\n");
            embed.appendDescription(String.format("این هفته %s با %s ساعت و %s دقیقه پلی تایم برنده شد",
                    pPlayerList.get(0).getUserName(), hours, minutes));
            embed.appendDescription("\n");
            embed.appendDescription("\n");
            embed.appendDescription(String.format(
                        "\uD83C\uDFC6 نفرات برتر این هفته:\n" +
                        "\uD83E\uDD47 %s\n" +
                        "\uD83E\uDD48 %s\n" +
                        "\uD83E\uDD49 %s\n",
                        pPlayerList.get(0).getUserName(),
                        pPlayerList.get(1).getUserName(),
                        pPlayerList.get(2).getUserName()
                    )
            );
            embed.appendDescription("\n");
            embed.appendDescription(
                    "\uD83D\uDD39 شما هم با پلی دادن و دریافت رتبه \uD83E\uDD47 " +
                    "در داخل سرور میتوانید در هر هفته برنده رنک **Baron** به مدت یک هفته بشوید !\n" +
                    "\n" +
                    "\uD83D\uDD39 Play.QPixel.IR\n" +
                    "\uD83D\uDFE3 QPixel.IR/Discord\n" +
                    "\uD83C\uDF10 wWw.QPixel.IR"
            );
            embed.setFooter("QPixel | PlayTime");

            /*eb.setAuthor(String.format("\uD83E\uDDED  PlayTime | %s", LocalDateTime.now()), null,
                    "https://cdn.discordapp.com/attachments/587612394768039947/901758237475487774/IMG_20211024_123512_497.jpg");*/

            embed.setThumbnail(String.format("http://cravatar.eu/avatar/%s/64.png", pPlayerList.get(0).getUserName()));

            assert playtimeChannel != null;
            playtimeChannel.sendMessageEmbeds(embed.build())
                    .append("<@&758758796167348285>")
                    .queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDailyMessage() {
        try {
            List<PPlayer> pPlayerList = SayanPlayTime.SQL.getDailyPlayTimes();
            for (PPlayer pPlayer : pPlayerList) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(String.format("\uD83E\uDDED %s Daily PlayTime | %s", pPlayer.getUserName(), DateUtils.getCurrentShamsidate()), null);
                embed.setColor(new Color(0x2F5FBE));
                long total_time = pPlayer.getTime();

                embed.appendDescription(String.format("Total Time: %s", formattedTime(total_time)));
                embed.appendDescription("\n");
                embed.appendDescription("\n");
                for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
                    String gameMode = server.getServerInfo().getName();
                    long gameModeTime = SayanPlayTime.SQL.getDailyPlayTime(pPlayer.getUuid(), gameMode);
                    if (gameModeTime > 0) {
                        embed.addField(String.format("%s: ", gameMode),
                                formattedTime(gameModeTime), true);
                    }
                }
                embed.setFooter("QPixel | PlayTime");

                embed.setThumbnail(String.format("http://cravatar.eu/avatar/%s/64.png", pPlayer.getUserName()));

                assert staffPlayTimeChannel != null;
                staffPlayTimeChannel.sendMessageEmbeds(embed.build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formattedTime(long total_time) {
        long seconds = total_time / 1000;
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        return String.format("%sh %sm", hours, minutes);
    }
}
