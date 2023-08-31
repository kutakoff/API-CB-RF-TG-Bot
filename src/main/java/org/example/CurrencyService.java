package org.example;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CurrencyService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String CB_RF_SITE = "cbr-xml-daily.ru";

    public String callService(String year, String month, String day, Country country) throws InvalidInputException, NotFoundCurrencyException {
        return currencyOut(year, month, day, country);
    }

    /**
     * Метод выводит валюту в читабельной форме
     *
     * @param year,month,day дата, на которую пользователь хочеь получить информацию
     * @param country        страна, на которую пользователь хочет получить данные о валюте
     * @return возвращает читабельную форму: название валюты/стоимость этой валюты в рублях на введёное пользователем дату
     **/
    private String currencyOut(String year, String month, String day, Country country) throws InvalidInputException, NotFoundCurrencyException {
        String url = "https://www." + CB_RF_SITE + "/archive/" + getDate(year, month, day) + "/daily_json.js"; //yyyy/mm/dd
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String currency = getCurrency(Objects.requireNonNull(restTemplate.getForObject(url, String.class)), country.name()); //использование API
        String[] splittingName = currency.split(","); //разделяем валюту на компоненты
        return splittingName[4].replace("\"Name\"", "Название валюты")
                + "\n\n" + splittingName[5].replace("\"Value\"", "Стоимость в рублях на " + getDate(year, month, day)).trim();
    }

    /**
     * Метод разделяет множество валют.
     *
     * @param allText           текст, возвращенный из API(включая инфу, помимо валюты)
     * @param userCurrencyChose валюта страны, которую вбил пользователь
     * @return возвращает валюту
     */
    private String getCurrency(String allText, String userCurrencyChose) throws NotFoundCurrencyException {
        String[] splitting = allText.split("\"Valute\": *"); //[0] - мусор, [1] - валюты
        String[] currencies = splitting[1].split("},"); //разделяем валюты
        for (String currency : currencies) {
            if (currency.contains(userCurrencyChose)) {
                return currency;
            }
        }
        throw new NotFoundCurrencyException(); //обработка в классе APICBRFActivity.java
    }

    /**
     * Метод устанавливает дату для дальнейшего получения API с сайта ЦБ РФ
     *
     * @return возвращает готовую дату для API в формате yyyy/mm/dd
     */
    private String getDate(String year, String month, String day) throws InvalidInputException {
        int intMonth = Integer.parseInt(month);
        int intDay = Integer.parseInt(day);
        if (year.length() != 4 || intMonth > 12 || intMonth < 1 || intDay > 31 || intDay < 1) {
            throw new InvalidInputException(); //обработка в классе APICBRFActivity.java
        }
        if (intMonth < 10) {
            month = "0" + intMonth;
        }
        if (intDay < 10) {
            day = "0" + intDay;
        }
        return new Date(year, month, day).toString();
    }
}