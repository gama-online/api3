package lt.gama.service;

import jakarta.validation.constraints.NotNull;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.ISeriesWithOrdinal;
import lt.gama.model.sql.base.BaseNumberDocumentSql;
import lt.gama.model.sql.entities.CounterSql;
import lt.gama.model.sql.entities.id.CounterId;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.service.repo.CounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.IllegalFormatException;

@Service
public class CounterService {

    private final Auth auth;
    private final CounterRepository counterRepository;

    public CounterService(Auth auth, CounterRepository counterRepository) {
        this.auth = auth;
        this.counterRepository = counterRepository;
    }

    public String format(long number, CounterDesc desc) {
        if (desc == null) {
            return String.valueOf(number);

        } else if (StringHelper.isEmpty(desc.getFormat())) {
            return (desc.getPrefix() == null ? "" : desc.getPrefix()) + number;

        } else {
            try {
                return StringHelper.isEmpty(desc.getPrefix()) ?
                        String.format(desc.getFormat(), number) :
                        String.format(desc.getFormat(), desc.getPrefix(), number);

            } catch (IllegalFormatException | NullPointerException e) {
                return (desc.getPrefix() == null ? "" : desc.getPrefix()) + number;
            }
        }
    }

    public ISeriesWithOrdinal next(@NotNull final CounterDesc desc) {
        return next(desc, 0);
    }

    @Transactional
    public ISeriesWithOrdinal next(@NotNull final CounterDesc desc, final int number) {
        final CounterId key = new CounterId(auth.getCompanyId(), desc);
        var counter = counterRepository.findById(key)
                .map(CounterSql::increment)
                .orElseGet(() -> new CounterSql(auth.getCompanyId(), desc, desc.getStart() != null ? desc.getStart() : 1));
        if (counter.getCount() < number) counter.setCount(number);
        counterRepository.save(counter);

        final long count = counter.getCount();
        return new ISeriesWithOrdinal() {
            @Override
            public String getNumber() {
                return format(count, desc);
            }

            @Override
            public String getSeries() {
                return desc.getPrefix();
            }

            @Override
            public Long getOrdinal() {
                return count;
            }
        };
    }

    public ISeriesWithOrdinal getNext(@NotNull final CounterDesc desc) {
        long count = getNextCount(desc);
        return new ISeriesWithOrdinal() {
            @Override
            public String getNumber() {
                return format(count, desc);
            }

            @Override
            public String getSeries() {
                return desc.getPrefix();
            }

            @Override
            public Long getOrdinal() {
                return count;
            }
        };
    }

    public int getNextCount(@NotNull final CounterDesc desc) {
        final CounterId key = new CounterId(auth.getCompanyId(), desc);
        var counter = counterRepository.findById(key);
        return counter.map(counterSql -> counterSql.getCount() + 1)
                .orElseGet(() -> (desc.getStart() != null ? desc.getStart() : 1));
    }

    public int getCount(@NotNull final CounterDesc desc) {
        final CounterId key = new CounterId(auth.getCompanyId(), desc);
        return counterRepository.findById(key)
                .map(CounterSql::getCount)
                .orElseGet(() -> desc.getStart() != null ? desc.getStart() - 1 : 0);
    }

    @Transactional
    public void setCount(@NotNull final CounterDesc desc) {
        final int start = desc.getStart() != null ? desc.getStart() : 1;
        final CounterId key = new CounterId(auth.getCompanyId(), desc);
        var counter = counterRepository.findById(key)
                .map(c -> c.update(start) )
                .orElseGet(() -> new CounterSql(auth.getCompanyId(), desc, start));
        counterRepository.save(counter);
    }

    public <E extends BaseNumberDocumentSql> E decodeDocNumber(E entity) {
        if (StringHelper.hasValue(entity.getNumber())) {
            ISeriesWithOrdinal seriesWithOrdinal = StringHelper.parseDocNumber(entity.getNumber());
            entity.setSeries(seriesWithOrdinal.getSeries());
            entity.setOrdinal(seriesWithOrdinal.getOrdinal());
        }
        // format number as "series ordinal", something like "ABC 42"
        if (entity.getOrdinal() != null && StringHelper.isEmpty(entity.getNumber())) {
            entity.setNumber((StringHelper.hasValue(entity.getSeries()) ? entity.getSeries() + " " : "") + entity.getOrdinal());
        }
        return entity;
    }
}
