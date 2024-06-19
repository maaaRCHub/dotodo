package com.bluecrimson.todo;

public class Task {
    private int id;
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String repeatOption;
    private String priorityOption;
    private boolean isSelected;

    public Task() {
    }

    public Task(int id, String title, String description, String startDateTime, String endDateTime, String repeatOption, String priorityOption) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.repeatOption = repeatOption;
        this.priorityOption = priorityOption;
    }

    public Task(String title, String description, String string, String string1, String selectedRepeatOption, String selectedPriorityOption) {
        this.title = title;
        this.description = description;
        this.startDateTime = string;
        this.endDateTime = string1;
        this.repeatOption = selectedRepeatOption;
        this.priorityOption = selectedPriorityOption;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getRepeatOption() {
        return repeatOption;
    }

    public void setRepeatOption(String repeatOption) {
        this.repeatOption = repeatOption;
    }

    public String getPriorityOption() { return priorityOption; }

    public void setPriorityOption(String priorityOption) {
        this.priorityOption = priorityOption;
    }

    public boolean getIsSelected(){ return isSelected; }

    public void setIsSelected(boolean isSelected){ this.isSelected = isSelected; }

    public boolean isAlarmSet() {
        return !startDateTime.isEmpty() && !endDateTime.isEmpty();
    }

    public boolean isRepeatSet() {
        return repeatOption != null && !repeatOption.isEmpty() && !repeatOption.equals("none");
    }

    public String getPriority() {
        return priorityOption;
    }


}
