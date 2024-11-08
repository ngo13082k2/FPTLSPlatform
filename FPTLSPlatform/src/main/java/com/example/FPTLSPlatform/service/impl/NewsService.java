package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.NewsDTO;
import com.example.FPTLSPlatform.model.News;
import com.example.FPTLSPlatform.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    public NewsDTO createNews(NewsDTO newsDTO) {
        News news = mapDTOToEntity(newsDTO);
        News savedNews = newsRepository.save(news);
        return mapEntityToDTO(savedNews);
    }

    public NewsDTO updateNews(Long id, NewsDTO newsDTO) {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        existingNews.setDate(newsDTO.getDate());
        existingNews.setTime(newsDTO.getTime());
        existingNews.setTitle(newsDTO.getTitle());
        existingNews.setContent(newsDTO.getContent());

        News updatedNews = newsRepository.save(existingNews);
        return mapEntityToDTO(updatedNews);
    }

    public List<NewsDTO> getAllNews() {
        return newsRepository.findAll().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        return mapEntityToDTO(news);
    }

    private News mapDTOToEntity(NewsDTO newsDTO) {
        return News.builder()
                .id(newsDTO.getId())
                .date(newsDTO.getDate())
                .time(newsDTO.getTime())
                .title(newsDTO.getTitle())
                .content(newsDTO.getContent())
                .build();
    }

    private NewsDTO mapEntityToDTO(News news) {
        return NewsDTO.builder()
                .id(news.getId())
                .date(news.getDate())
                .time(news.getTime())
                .title(news.getTitle())
                .content(news.getContent())
                .build();
    }
}
