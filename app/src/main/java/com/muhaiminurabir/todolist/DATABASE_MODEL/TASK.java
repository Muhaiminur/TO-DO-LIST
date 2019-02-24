package com.muhaiminurabir.todolist.DATABASE_MODEL;

public class TASK {
    String title;
    String update;

    public TASK() {
    }

    public TASK(String title, String update) {
        this.title = title;
        this.update = update;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "TASK{" +
                "title='" + title + '\'' +
                ", update='" + update + '\'' +
                '}';
    }
}
