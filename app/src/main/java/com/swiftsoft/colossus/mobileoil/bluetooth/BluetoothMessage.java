package com.swiftsoft.colossus.mobileoil.bluetooth;

/**
 * Created by Alan on 13/10/2015.
 */
public class BluetoothMessage
{
    public enum Direction
    {
        Incoming,
        Outgoing
    }

    private Direction messageDirection;

    public Direction getMessageDirection()
    {
        return this.messageDirection;
    }

    public void setMessageDirection(Direction direction)
    {
        this.messageDirection = direction;
    }

    private String messageContent;

    public String getMessageContent()
    {
        return this.messageContent;
    }

    public void setMessageContent(String message)
    {
        this.messageContent = message;
    }

    private long creationDate;

    public long getMessageDate()
    {
        return this.creationDate;
    }

    public void setMessageDate(long date)
    {
        this.creationDate = date;
    }

    public BluetoothMessage(Direction direction, String content, long date)
    {
        this.messageDirection = direction;
        this.messageContent = content;
        this.creationDate = date;
    }
}
