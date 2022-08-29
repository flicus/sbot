package org.schors.sbot.services;

import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;
import org.schors.sbot.atom.Feed;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class FeedService {

    private Cache<String, String> urlCache;

    public Optional<InlineKeyboardButton> getControl(Feed feed, String type) {
        return getControl(feed, type, type);
    }

    public Optional<InlineKeyboardButton> getControl(Feed feed, String name, String type) {
        return feed.getLink().stream()
                .filter(link -> link.getRel().equalsIgnoreCase(type))
                .findFirst()
                .map(link -> {
                    String id = Integer.toHexString(link.getHref().hashCode());
                    urlCache.put(id, link.getHref());
                    return InlineKeyboardButton.builder().text(name).callbackData(id).build();
                });
    }

    public interface MessageUpdater<T> {
        void update(T t);
    }

    public Flux<BotApiMethodSerializable> editMessage(Integer msgId, Feed feed, Consumer<MessageUpdater> consumer) {
        EditMessageText.EditMessageTextBuilder textBuilder = EditMessageText.builder()
                .messageId(msgId)
                .text(composeMessage(feed))
                .parseMode("html");
        consumer.accept((MessageUpdater<Long>) id -> textBuilder.chatId(id));

        EditMessageReplyMarkup.EditMessageReplyMarkupBuilder keyboardBuilder = EditMessageReplyMarkup.builder()
                .messageId(msgId)
                .replyMarkup(composeKeyboard(feed));
        consumer.accept((MessageUpdater<Long>) id -> keyboardBuilder.chatId(id));

        return Flux.just(textBuilder.build(), keyboardBuilder.build());
    }

    public Flux<BotApiMethodMessage> getMessage(Feed feed, Consumer<MessageUpdater> consumer) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .text(composeMessage(feed))
                .parseMode("html")
                .replyMarkup(composeKeyboard(feed));
        consumer.accept((MessageUpdater<Long>) id -> builder.chatId(id));

        return Flux.just(builder.build());
    }

    private InlineKeyboardMarkup composeKeyboard(Feed feed) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        getControl(feed, "Назад", "up").ifPresent(button -> buttons.add(button));
        getControl(feed, "Дальше", "next").ifPresent(button -> buttons.add(button));
        return InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
    }

    private String composeMessage(Feed feed) {
        StringBuilder result = new StringBuilder();
        if (feed.getTitle() != null) {
            result.append("<b>").append(feed.getTitle()).append("</b>\n");
        }

        if (feed.getEntry() != null && feed.getEntry().size() > 0) {
            feed.getEntry().forEach(entry -> {
                result.append("<b>").append(entry.getTitle()).append("</b>");
                if (entry.getAuthor() != null) {
                    result.append(" (").append(entry.getAuthor().getName()).append(")");
                }
                result.append("\n");
                entry.getLink().stream()
                        .filter(link -> link.getType() != null && link.getType().toLowerCase().contains("opds-catalog"))
                        .forEach(link -> {
                            if (link.getTitle() != null) {
                                result.append(link.getTitle());
                            }
                            String id = Integer.toHexString(link.getHref().hashCode());
                            urlCache.put(id, link.getHref());
                            result.append(" /c").append(id).append("\n");
                        });
                entry.getLink().stream()
                        .filter(link -> link.getRel() != null && link.getRel().contains("open-access"))
                        .forEach(link -> {
                            String type = link.getType().replace("application/", "");
                            result.append(type);
                            String id = Integer.toHexString(link.getHref().hashCode());
                            urlCache.put(id, link.getHref());
                            result.append(" : /d").append(id).append("\n");
                            if ("fb2+zip".equals(type)) {
                                result.append("fb2").append(" : /z").append(id).append("\n");
                            }
                        });
                result.append("\n");
//                builder.text(result.getParts().get(0));
            });
        } else {
            result.append("Ничего не найдено");
        }
        return result.toString();
    }
}
