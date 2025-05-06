package com.telegram_bots.bookbot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


@Service
public class LitresService {

    private static final int CHARACTERS_PER_PAGE = 1800;

    public Document fetchDocument(String query) throws IOException {
        String url = "https://www.litres.ru/pages/rmd_search/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return Jsoup.connect(url).get();
    }

    public List<LitresBookDto> searchBooks(String query) {
        List<LitresBookDto> result = new ArrayList<>();
        try {
            Document doc = fetchDocument(query);
            Elements bookBlocks = doc.select(".art-item");

            for (Element book : bookBlocks) {
                String title = book.select(".art-item__name a").text();
                String author = book.select(".art-item__author a").text();

                String annotation = book.select(".art-item__annotation").text();

                if (annotation.isEmpty()) {
                    Element annotationElement = book.selectFirst("div[data-test-id=annotation]");
                    if (annotationElement != null) {
                        annotation = annotationElement.text();
                    }
                }

                int totalPages = 0;
                // Пример строки: "Объем: 350 тыс. знаков"
                Pattern pattern = Pattern.compile("Объем:\\s*(\\d+)\\s*тыс\\. знаков");
                Matcher matcher = pattern.matcher(annotation);

                if (matcher.find()) {
                    int thousands = Integer.parseInt(matcher.group(1));
                    int totalChars = thousands * 1000;
                    totalPages = (int) Math.ceil((double) totalChars / CHARACTERS_PER_PAGE);
                }

                if (!title.isEmpty()) {
                    result.add(new LitresBookDto(title, author, totalPages));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
