package lt.gama.test.tools;

import lt.gama.model.type.inventory.PlPrice;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComparatorTest {

    @Test
    void testDateComparator() {
        assertThat(Comparator.nullsFirst(LocalDate::compareTo).compare(
                new PlPrice(null, null, null).getDateFrom(),
                new PlPrice(null, null, null).getDateFrom())).isEqualTo(0);

        assertThat(Comparator.nullsFirst(LocalDate::compareTo).compare(
                new PlPrice(LocalDate.of(2018, 1, 5), null, null).getDateFrom(),
                new PlPrice(LocalDate.of(2018, 1, 5), null, null).getDateFrom())).isEqualTo(0);

        assertThat(Comparator.nullsFirst(LocalDate::compareTo).compare(
                new PlPrice(LocalDate.of(2018, 1, 5), null, null).getDateFrom(),
                new PlPrice(null, LocalDate.of(2018, 1, 5), null).getDateFrom())).isEqualTo(1);

        assertThat(Comparator.nullsFirst(LocalDate::compareTo).compare(
                new PlPrice(null, LocalDate.of(2018, 1, 5), null).getDateFrom(),
                new PlPrice(LocalDate.of(2018, 1, 5), null, null).getDateFrom())).isEqualTo(-1);
    }

    @Test
    void testComparatorsChain() {
        List<DocTest> documents = new ArrayList<>();
        documents.add(new DocTest(1, LocalDate.of(2018, 1, 5), "A1"));
        documents.add(new DocTest(2, LocalDate.of(2018, 1, 3), "B1"));
        documents.add(new DocTest(3, LocalDate.of(2018, 1, 3), "B2"));

        documents.sort(Comparator.comparing(DocTest::getDate).thenComparing(DocTest::getNumber));

        assertThat(documents.get(0).getId()).isEqualTo(2);
        assertThat(documents.get(1).getId()).isEqualTo(3);
        assertThat(documents.get(2).getId()).isEqualTo(1);
    }

    @Test
    void testComparatorsChainWithNulls() {
        List<DocTest> documents = new ArrayList<>();
        documents.add(new DocTest(1, LocalDate.of(2018, 1, 5), "A1"));
        documents.add(new DocTest(2, LocalDate.of(2018, 1, 3), null));
        documents.add(new DocTest(3, LocalDate.of(2018, 1, 3), "B2"));
        documents.add(new DocTest(4, null, null));
        documents.add(new DocTest(5, null, "B2"));

        documents.sort(Comparator.comparing(DocTest::getDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(DocTest::getNumber, Comparator.nullsFirst(Comparator.naturalOrder())));

        assertThat(documents.get(0).getId()).isEqualTo(4);
        assertThat(documents.get(1).getId()).isEqualTo(5);
        assertThat(documents.get(2).getId()).isEqualTo(2);
        assertThat(documents.get(3).getId()).isEqualTo(3);
        assertThat(documents.get(4).getId()).isEqualTo(1);

        documents.sort(Comparator.comparing(DocTest::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DocTest::getNumber, Comparator.nullsLast(Comparator.naturalOrder())));

        assertThat(documents.get(0).getId()).isEqualTo(3);
        assertThat(documents.get(1).getId()).isEqualTo(2);
        assertThat(documents.get(2).getId()).isEqualTo(1);
        assertThat(documents.get(3).getId()).isEqualTo(5);
        assertThat(documents.get(4).getId()).isEqualTo(4);
    }

    static class DocTest {

        private int id;

        private LocalDate date;

        private String number;

        DocTest(int id, LocalDate date, String number) {
            this.id = id;
            this.date = date;
            this.number = number;
        }

        // generated

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "DocTest{" +
                    "id=" + id +
                    ", date=" + date +
                    ", number='" + number + '\'' +
                    '}';
        }
    }


}


