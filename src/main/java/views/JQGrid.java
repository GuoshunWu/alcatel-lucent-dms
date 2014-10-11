package views;

import java.util.*;

/**
 * Created by guoshunw on 2014/9/26.
 */
public class JQGrid {
    // page number
    private int page = 0;
    // total page number
    private int total = 0;
    // total record number
    private int records = 0;

    private String idProp = "id";

    private Collection<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

    public Map<String, Object> addRow(Collection<String> cells) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("cell", cells);
        rows.add(row);
        return row;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public String getIdProp() {
        return idProp;
    }

    public void setIdProp(String idProp) {
        this.idProp = idProp;
    }

    public Collection<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(Collection<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
