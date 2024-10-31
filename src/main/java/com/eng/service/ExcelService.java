package com.eng.service;

import com.eng.domain.Meaning;
import com.eng.domain.Word;
import com.eng.repository.MeanRepository;
import com.eng.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {
    /*
    단어는 중복이지만 뜻은 중복이 아닐 수 있듯 하나의 method에서 처리x
    각각 method에서 따로 처리해야함
     */
    private final WordRepository wordRepository;
    private final MeanRepository meanRepository;

    public void saveWord(MultipartFile file) throws IOException {
        // workbook : 하나의 엑셀 파일을 의미
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0); // 첫번째 sheet
        Set<Word> words = new HashSet<>();
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next(); // 첫 행(제목) 건너 뛰기
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Word word = Word.builder()
                    .word(row.getCell(0).getStringCellValue())
                    .build();
            words.add(word);
        }
        // DB에 저장(중복 try~catch)
        try {
            wordRepository.saveAll(words);
        } catch (DataIntegrityViolationException e) {

        }
    }

    public void saveMeaning(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0); // 첫번째 sheet
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>(); // key 중복
        Iterator<Row> rowIterator = sheet.iterator();
        List<Meaning> list = new ArrayList<>();
        if (rowIterator.hasNext()) {
            rowIterator.next(); // 첫 행(제목) 건너 뛰기
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String wordText = row.getCell(0).getStringCellValue();
            String meaningText = row.getCell(1).getStringCellValue();

            // 해당 key에 해당하는 값 중복 체크 (1차 중복 체크 - 메모리)
            map.putIfAbsent(wordText, new ArrayList<>());
            if(!Objects.requireNonNull(map.get(wordText)).contains(meaningText)){
                map.add(wordText,meaningText);
                Word word = wordRepository.findByWord(wordText).orElseThrow();

                // word와 meaning이 같이 중복인 경우는 새로 추가하지 않음(2차 중복 체크 - DB)
                int exists = meanRepository.existsByWordAndMeaning(word.getId(), meaningText); // (2차 체크)
                if (exists==0) {
                    list.add(Meaning.createMeaning(word, meaningText));
                }
            }
        }
        // 일괄 저장
        meanRepository.saveAll(list);
    }
}
