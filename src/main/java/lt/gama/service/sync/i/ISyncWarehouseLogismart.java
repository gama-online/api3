package lt.gama.service.sync.i;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.service.sync.i.base.ISyncWarehouseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ISyncWarehouseLogismart extends ISyncWarehouseService {

    class Response {
        private boolean status;
        private String error;
        private Map<String, String> errors;

        public List<String> getAllErrors() {
            List<String> allErrors = new ArrayList<>();
            if (StringHelper.hasValue(error)) allErrors.add(error);
            if (CollectionsHelper.hasValue(errors)) allErrors.addAll(
                    errors.entrySet()
                            .stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .toList());
            return allErrors;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public void setErrors(Map<String, String> errors) {
            this.errors = errors;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "status=" + status +
                    ", error='" + error + '\'' +
                    ", errors=" + errors +
                    '}';
        }
    }

    class ArrivalResponse extends Response {
        private Long arrival_id;

        public Long getArrival_id() {
            return arrival_id;
        }

        public void setArrival_id(Long arrival_id) {
            this.arrival_id = arrival_id;
        }

        @Override
        public String toString() {
            return "ArrivalResponse{" +
                    "arrival_id=" + arrival_id +
                    "} " + super.toString();
        }
    }

    class OrderResponse extends Response {
        private Long order_id;

        public Long getOrder_id() {
            return order_id;
        }

        public void setOrder_id(Long order_id) {
            this.order_id = order_id;
        }

        @Override
        public String toString() {
            return "ArrivalResponse{" +
                    "order_id=" + order_id +
                    "} " + super.toString();
        }
    }

    record Paging (
            @JsonProperty("next_page") boolean nextPage,
            @JsonProperty("page") int page,
            @JsonProperty("page_count") int pageCount,
            @JsonProperty("per_page") int perPage,
            @JsonProperty("previous_page") boolean previousPage,
            @JsonProperty("total_count") int totalCount
    ) {}
}
