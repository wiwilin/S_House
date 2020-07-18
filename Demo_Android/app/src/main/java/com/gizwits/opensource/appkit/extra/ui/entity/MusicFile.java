package com.gizwits.opensource.appkit.extra.ui.entity;

/**
 * 音乐文件对应实体类
 */
public class MusicFile {
    private String dir;//音乐绝对路径
    private String name;//音乐名称
    private int musicDuration;//音乐时长

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public String getName() {
        return name;
    }

    public int getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(int musicDuration) {
        this.musicDuration = musicDuration;
    }

    @Override
    public String toString() {
        return "MusicFile{" +
                "dir='" + dir + '\'' +
                ", name='" + name + '\'' +
                ", musicDuration=" + musicDuration +
                '}';
    }
}
