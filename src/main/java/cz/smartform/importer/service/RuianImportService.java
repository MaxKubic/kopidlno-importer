package cz.smartform.importer.service;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.smartform.importer.entity.CastObce;
import cz.smartform.importer.entity.Obec;
import cz.smartform.importer.repository.CastObceRepository;
import cz.smartform.importer.repository.ObecRepository;

@Service
public class RuianImportService {

    private static final Logger log = LoggerFactory.getLogger(RuianImportService.class);

    private final ObecRepository obecRepository;
    private final CastObceRepository castObceRepository;

    @Value("${app.import-url:https://www.smartform.cz/download/kopidlno.xml.zip}")
    private String importUrl;

    public RuianImportService(ObecRepository obecRepository, CastObceRepository castObceRepository) {
        this.obecRepository = obecRepository;
        this.castObceRepository = castObceRepository;
    }

    @Transactional
    public void importData() throws Exception {
        log.info("Stahuji soubor z URL: {}", importUrl);

        URI uri = URI.create(importUrl);
        try (InputStream httpStream = uri.toURL().openStream();
             ZipInputStream zipStream = new ZipInputStream(httpStream)) {

            ZipEntry entry;
            boolean xmlFound = false;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    log.info("Zpracovávám XML soubor: {}", entry.getName());
                    parseAndPersist(zipStream);
                    xmlFound = true;
                    break;
                }
            }

            if (!xmlFound) {
                throw new IllegalStateException("V ZIP archivu nebyl nalezen žádný XML soubor.");
            }
        }
    }

    private void parseAndPersist(InputStream xmlStream) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        XMLStreamReader reader = factory.createXMLStreamReader(xmlStream);

        Map<Integer, Obec> obceMap = new HashMap<>();
        List<CastObceRaw> castiRawList = new ArrayList<>();

        Deque<String> elementStack = new ArrayDeque<>();
        StringBuilder textBuffer = new StringBuilder();

        Integer obecKod = null;
        String obecNazev = null;

        Integer coKod = null;
        String coNazev = null;
        Integer coObecKod = null;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    textBuffer.setLength(0);
                    elementStack.push(reader.getLocalName());
                }
                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
                    textBuffer.append(reader.getText().trim());
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    String closedTag = reader.getLocalName();
                    String text = textBuffer.toString();

                    if (!elementStack.isEmpty()) {
                        elementStack.pop();
                    }
                    String parentTag = elementStack.peek();

                    if ("Obec".equals(parentTag)) {
                        if ("Kod".equalsIgnoreCase(closedTag)) obecKod = Integer.valueOf(text);
                        if ("Nazev".equalsIgnoreCase(closedTag)) obecNazev = text;
                    } else if ("Obec".equals(closedTag)) {
                        if (obecKod != null && obecNazev != null) {
                            obceMap.put(obecKod, new Obec(obecKod, obecNazev));
                        }
                        obecKod = null;
                        obecNazev = null;
                    }

                    if ("CastObce".equals(parentTag)) {
                        if ("Kod".equalsIgnoreCase(closedTag)) coKod = Integer.valueOf(text);
                        if ("Nazev".equalsIgnoreCase(closedTag)) coNazev = text;
                    } else if ("Obec".equals(parentTag) && "CastObce".equals(getGrandParentTag(elementStack))) {
                        if ("Kod".equalsIgnoreCase(closedTag)) coObecKod = Integer.valueOf(text);
                    } else if ("CastObce".equals(closedTag)) {
                        if (coKod != null && coNazev != null && coObecKod != null) {
                            castiRawList.add(new CastObceRaw(coKod, coNazev, coObecKod));
                        }
                        coKod = null;
                        coNazev = null;
                        coObecKod = null;
                    }
                }
            }
        }


        List<Obec> obceList = new ArrayList<>(obceMap.values());
obecRepository.saveAll(obceList);
        log.info("Úspěšně uloženo {} obcí.", obceMap.size());

        List<CastObce> castiToSave = new ArrayList<>();
        for (CastObceRaw raw : castiRawList) {
            Obec obec = obceMap.get(raw.obecKod());
            if (obec == null) {
                obec = obecRepository.findById(Objects.requireNonNull(raw.obecKod()))
        .orElseThrow(() -> new IllegalStateException("Nenalezena obec s kódem: " + raw.obecKod()));
            }
            castiToSave.add(new CastObce(raw.kod(), raw.nazev(), obec));
        }

        List<CastObce> castiToSaveList = new ArrayList<>(castiToSave);
        castObceRepository.saveAll(castiToSaveList);
        log.info("Úspěšně uloženo {} částí obce.", castiToSaveList.size());
    }

    private String getGrandParentTag(Deque<String> stack) {
        if (stack.size() < 2) return null;
        Iterator<String> it = stack.iterator();
        it.next();
        return it.next();
    }

    private record CastObceRaw(Integer kod, String nazev, Integer obecKod) {}
}