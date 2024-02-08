package com.epam.service;

import com.epam.dto.MetadataDto;
import com.epam.entity.Mp3File;
import com.epam.repository.ResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ResourceService {

    private final RestTemplate restTemplate;
    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(RestTemplate restTemplate, ResourceRepository resourceRepository) {
        this.restTemplate = restTemplate;
        this.resourceRepository = resourceRepository;
    }

    public Integer saveFile(MultipartFile multipartFile) throws IOException {
        Mp3File mp3File = new Mp3File();
        mp3File.setName(multipartFile.getOriginalFilename());
        mp3File.setData(multipartFile.getBytes());

        try {
            getMetadata(multipartFile);
        } catch (TikaException | SAXException e ) {
            throw new RuntimeException(e);
        }

        Mp3File savedFile = resourceRepository.save(mp3File);
        Integer savedFileId = savedFile.getId();

        try {
            saveSongMetadata(multipartFile, savedFileId);
        } catch (Exception e) {
            log.error("Error while parsing the mp3 file", e);
        }



        return savedFileId;
    }

    public byte[] getFileBytes(Integer fileId) {
        return resourceRepository.findById(fileId).map(Mp3File::getData).orElse(null);
    }

    public List<Integer> deleteFiles(String ids) {
        return Arrays.stream(ids.split(","))
                .map(Integer::valueOf)
                .filter(resourceRepository::existsById)
                .peek(resourceRepository::deleteById).toList();
    }

    private void saveSongMetadata(MultipartFile multipartFile, Integer id) throws IOException, TikaException, SAXException {
        Metadata metadata = getMetadata(multipartFile);
        MetadataDto metadataDto = buildMetadata(metadata, id);
        restTemplate.postForEntity("http://SONG-SERVICE/api/v1/song", metadataDto, Integer.class);
    }

    private Metadata getMetadata(MultipartFile multipartFile) throws IOException, TikaException, SAXException {
        Mp3Parser mp3Parser = new Mp3Parser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        mp3Parser.parse(multipartFile.getInputStream(), handler, metadata, context);
        return metadata;
    }

    private MetadataDto buildMetadata(Metadata metadata, Integer resourceId) {
        return MetadataDto.builder()
                .year(metadata.get("xmpDM:releaseDate"))
                .name(metadata.get("dc:title"))
                .album(metadata.get("xmpDM:album"))
                .length(getDurationInMinutes(metadata.get("xmpDM:duration")))
                .resourceId(resourceId)
                .build();
    }

    private String getDurationInMinutes(String duration) {
        int value = Integer.parseInt(duration.substring(0, duration.indexOf(".")));
        return (value / 60) + ":" + (value % 60);
    }


}
