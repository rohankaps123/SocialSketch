package com.example.kiwitech.socialsketch.DataTypes;

/**  A SocialSketch Room class
 * @author Rohan Kapoor
 * @since 1.0
 */
public class SSRoom {
    private String name;
    private String createdBY;

    public SSRoom(String name, String createdBY){
        this.name = name;
        this.createdBY = createdBY;
    }


    public String getName() {
        return name;
    }

    public String getcreatedBY() {
        return createdBY;
    }

}
