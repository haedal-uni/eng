package com.eng.service;

import com.eng.domain.Word;
import com.eng.repository.ExcelRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExcelService {
    /*
    단어는 중복이지만 뜻은 중복이 아닐 수 있듯 하나의 method에서 처리x
    각각 method에서 따로 처리해야함
     */
    private final ExcelRepository repository;

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
            repository.saveAll(words);
        } catch (DataIntegrityViolationException e) {

        }
    }
}
