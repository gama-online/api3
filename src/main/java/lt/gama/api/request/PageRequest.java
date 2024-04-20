package lt.gama.api.request;

import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageRequest {

	public static final int MAX_PAGE_SIZE = 99999;

	private Integer cursor;

	private int pageSize = 10;

	private int total = -1;

	private boolean backward = false;

	private String filter;

	private String order;

	private String label;

	private boolean dateRange;

	private LocalDate dateFrom;

	private LocalDate dateTo;

	private List<PageRequestCondition> conditions = new ArrayList<>();

	private boolean refresh = false;


	private Long parentId;

	private String parentName;

	private Map<String, String> parentObj;


	/**
	 * If pageSize can be changed in methods.
	 * if false (default) - then in query methods page size will be fixed to fit the range 10-50.
	 */
	private boolean fixedPageSize;

    /**
     * Return no detail info, only master record
     */
	private boolean noDetail;


//	public int getSqlCursor() {
//		int cursor;
//		if (getCursor() == null) {
//			cursor = 0;
//		} else {
//			cursor = Integer.parseInt(getCursor());
//		}
//
//		if (isBackward() && cursor >= getPageSize()) {
//			cursor = cursor - getPageSize();
//		}
//		return cursor;
//	}

	// generated

	public Integer getCursor() {
		return cursor;
	}

	public void setCursor(Integer cursor) {
		this.cursor = cursor;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public boolean isBackward() {
		return backward;
	}

	public void setBackward(boolean backward) {
		this.backward = backward;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isDateRange() {
		return dateRange;
	}

	public void setDateRange(boolean dateRange) {
		this.dateRange = dateRange;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}

	public List<PageRequestCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<PageRequestCondition> conditions) {
		this.conditions = conditions;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public Map<String, String> getParentObj() {
		return parentObj;
	}

	public void setParentObj(Map<String, String> parentObj) {
		this.parentObj = parentObj;
	}

	public boolean isFixedPageSize() {
		return fixedPageSize;
	}

	public void setFixedPageSize(boolean fixedPageSize) {
		this.fixedPageSize = fixedPageSize;
	}

	public boolean isNoDetail() {
		return noDetail;
	}

	public void setNoDetail(boolean noDetail) {
		this.noDetail = noDetail;
	}


	@Override
	public String toString() {
		return "PageRequest{" +
				"cursor=" + cursor +
				", pageSize=" + pageSize +
				", total=" + total +
				", backward=" + backward +
				", filter='" + filter + '\'' +
				", order='" + order + '\'' +
				", label='" + label + '\'' +
				", dateRange=" + dateRange +
				", dateFrom=" + dateFrom +
				", dateTo=" + dateTo +
				", conditions=" + conditions +
				", refresh=" + refresh +
				", parentId=" + parentId +
				", parentName='" + parentName + '\'' +
				", parentObj=" + parentObj +
				", fixedPageSize=" + fixedPageSize +
				", noDetail=" + noDetail +
				"}";
	}
}
