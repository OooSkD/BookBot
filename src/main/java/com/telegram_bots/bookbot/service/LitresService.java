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


@Service
public class LitresService {
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

                if (!title.isEmpty()) {
                    result.add(new LitresBookDto(title, author));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
