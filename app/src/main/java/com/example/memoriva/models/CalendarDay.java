package com.example.memoriva.models;

/**
 * Represents a single day cell in the calendar grid.
 */
public class CalendarDay {

    private String dateString;   // ISO format YYYY-MM-DD, or empty for padding cells
    private int dayNumber;       // 1-31, or 0 for padding cells
    private boolean hasMemory;
    private boolean isSelected;
    private boolean isToday;
    private boolean isPadding;   // true for empty cells before the 1st of the month

    public CalendarDay() {
    }

    public CalendarDay(String dateString, int dayNumber, boolean hasMemory,
                       boolean isSelected, boolean isToday, boolean isPadding) {
        this.dateString = dateString;
        this.dayNumber = dayNumber;
        this.hasMemory = hasMemory;
        this.isSelected = isSelected;
        this.isToday = isToday;
        this.isPadding = isPadding;
    }

    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public boolean isHasMemory() { return hasMemory; }
    public void setHasMemory(boolean hasMemory) { this.hasMemory = hasMemory; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isToday() { return isToday; }
    public void setToday(boolean today) { isToday = today; }

    public boolean isPadding() { return isPadding; }
    public void setPadding(boolean padding) { isPadding = padding; }
}
