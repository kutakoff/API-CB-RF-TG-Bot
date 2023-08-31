package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class APICBRFActivity {

    private static final String TOKEN = "6417961114:AAFMxlnwVVKE08OQ0mpo3n2R1S3x4kIJd6s";
    public static TelegramBot bot = new TelegramBot(TOKEN);

    public static Chat chat;
    public static Long chatId;

    public static void main(String[] args) {
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                chat = update.message().chat();
                chatId = update.message().chat().id();
                String text = update.message().text();
                if (update.message() != null && update.message().chat() != null) {
                    try {
                        checkUserIn(text, bot);
                    } catch (InvalidInputException e) {
                        bot.execute(new SendMessage(chatId, "Ошибка в верности указанной даты. Требуемый формат: yyyy/mm/dd."));
                    } catch (NumberFormatException e) {
                        bot.execute(new SendMessage(chatId, "Ошибка в верности указанной даты. Вводите только цифры. Требуемый формат: yyyy/mm/dd."));
                    } catch (NotFoundCurrencyException | IllegalArgumentException e) {
                        bot.execute(new SendMessage(chatId, "Ошибка. Валюты такой страны нет."));
                    } catch (HttpClientErrorException e) {
                        bot.execute(new SendMessage(chatId, "Курс ЦБ РФ на данную дату не установлен. Проверить: https://www.cbr.ru/currency_base/daily."));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        bot.execute(new SendMessage(chatId, "Недостающий элемент в воде. Вам нужно отправить сообщение типа: год/месяц/число/кодстраны в формате yyyy/mm/dd/код."));
                    }
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void checkUserIn(String text, TelegramBot bot) throws InvalidInputException, NotFoundCurrencyException {
        if (text.equals("/start")) {
            bot.execute(new SendMessage(chatId, "Чтобы начать пользоваться ботом, вам нужно отправить сообщение типа: год/месяц/число/кодстраны в формате yyyy/mm/dd/код. \n\nВот всевозможные коды стран, которые вы можете ввести: \nAUD - Австралийский доллар\n AZN - Азербайджанский манат\n GBP - Фунт стерлингов Соединенного королевства\n AMD - Армянских драмов\n BYN - Белорусский рубль\n BGN - Болгарский лев\n BRL - Бразильскийреал\n HUF - Венгерских форинтов\n HKD - Гонконгских долларов\n DKK - Датских крон\n USD - Доллар США\\n EUR - Евро\n INR - Индийских рупий\n KZT - Казахстанских тенге\n CAD - Канадский доллар\n KGS - Киргизских сомов\n CNY - Китайских юаней\n MDL - Молдавских леев\n NOK - Норвежских крон\n PLN - Польский злотый\n RON - Румынский лей\n XDR - СДР (специальные права заимствования)\n SGD - Сингапурский доллар\n TJS - Таджикских сомони\n TRY - Турецкая лира\n TMT - Новый туркменский манат\n UZS - Узбекских сумов\n UAH - Украинских гривен\n CZK - Чешских крон\n SEK - Шведских крон\n CHF - Швейцарский франк\n ZAR - Южноафриканских рэндов\n KRW - Вон Республики Корея\n JPY - Японских иен"));
        } else {
            CurrencyService currencyService = new CurrencyService();
            String[] userInSplit = text.split("/");
            bot.execute(new SendMessage(chatId, currencyService.callService(userInSplit[0], userInSplit[1], userInSplit[2], Country.valueOf(userInSplit[3].toUpperCase()))));
        }
    }
}