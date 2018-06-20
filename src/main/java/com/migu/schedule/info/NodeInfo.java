package com.migu.schedule.info;

/**
 * @author Julia Hong
 *
 */
public class NodeInfo
{
    private int nodeId;

    private int totalConsumption;
    
    private int amount;

    public int getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(int nodeId)
    {
        this.nodeId = nodeId;
    }

    public int getTotalConsumption()
    {
        return totalConsumption;
    }

    public void setTotalConsumption(int totalConsumption)
    {
        this.totalConsumption = totalConsumption;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    @Override
    public String toString()
    {
        return "NodeInfo [nodeId=" + nodeId + ", totalConsumption=" + totalConsumption + ", amount=" + amount + "]";
    }
}
