package com.migu.schedule;


import com.migu.schedule.constants.ReturnCodeKeys;
import com.migu.schedule.info.NodeInfo;
import com.migu.schedule.info.TaskConsumption;
import com.migu.schedule.info.TaskInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/*
*类名和方法不能修改
 */
public class Schedule {
    
    /**
     * 注册节点节点统计信息
     */
    private List<NodeInfo> nodeInfoList=new LinkedList<NodeInfo>();
    
    /**
     * 任务信息统计列表
     */
    private List<TaskConsumption> taskList = new LinkedList<TaskConsumption>();

    public int init() {
        nodeInfoList.clear();
        taskList.clear();
        return ReturnCodeKeys.E001;
    }


    public int registerNode(int nodeId) {
        if (nodeId <= 0)
        {
            return ReturnCodeKeys.E004;
        }
        for(NodeInfo nodeInfo:nodeInfoList)
        {
            if(nodeInfo.getNodeId()==nodeId)
            {
                return ReturnCodeKeys.E005;                
            }
        }
        int index = -1;
        if (nodeInfoList.size() > 0)
        {
            for (int i = 0; i < nodeInfoList.size(); i++)
            {
                if (nodeId < nodeInfoList.get(i).getNodeId())
                {
                    index = i;
                }
            }
            if (index == -1)
            {
                NodeInfo nodeInfo=new NodeInfo();
                nodeInfo.setNodeId(nodeId);
                nodeInfoList.add(nodeInfo);
            }
            else
            {
                NodeInfo nodeInfo=new NodeInfo();
                nodeInfo.setNodeId(nodeId);
                nodeInfoList.add(index,nodeInfo);
            }
        }
        else
        {
            NodeInfo nodeInfo=new NodeInfo();
            nodeInfo.setNodeId(nodeId);
            nodeInfoList.add(nodeInfo);
        }
        return ReturnCodeKeys.E003;
    }

    public int unregisterNode(int nodeId) {
        if (nodeId <= 0)
        {
            return ReturnCodeKeys.E004;
        }
        NodeInfo nodeinfo = null;
        for (NodeInfo nodeInfo : nodeInfoList)
        {
            if (nodeInfo.getNodeId() == nodeId)
            {
                nodeinfo = nodeInfo;
            }
        }
        if (nodeinfo == null)
        {
            return ReturnCodeKeys.E007;
        }
        for (TaskConsumption task : taskList)
        {
            if (task.getNodeId() == nodeId)
            {
                task.setTaskId(0);
            }
        }
        nodeInfoList.remove(nodeinfo);
        return ReturnCodeKeys.E006;
    }


    public int addTask(int taskId, int consumption) {
        if (taskId <= 0)
        {
            return ReturnCodeKeys.E009;
        }
        for (TaskConsumption task : taskList)
        {
            if (task.getTaskId() == taskId)
            {
                return ReturnCodeKeys.E010;
            }
        }
        TaskConsumption task = new TaskConsumption();
        task.setTaskId(taskId);
        task.setConsumption(consumption);
        task.setNodeId(-1);
        taskList.add(task);
        return ReturnCodeKeys.E008;
    }


    public int deleteTask(int taskId) {
        if (taskId <= 0)
        {
            return ReturnCodeKeys.E009;
        }
        for (TaskConsumption task : taskList)
        {
            if (task.getTaskId() == taskId)
            {
                taskList.remove(task);
                return ReturnCodeKeys.E011;
            }
        }
        return ReturnCodeKeys.E012;
    }


    public int scheduleTask(int threshold) {
        // TODO 方法未实现
        if (threshold <= 0)
        {
            return ReturnCodeKeys.E002;
        }
        double aveCon = 0;// 平均负载
        int totalcon = 0;// 总负载
        for (TaskConsumption task : taskList)
        {
            totalcon += task.getConsumption();
        }
        aveCon = totalcon / nodeInfoList.size();
        // 先将任务列按先负载逆序后taskId排序
        Comparator<TaskConsumption> taskCpr = new Comparator<TaskConsumption>()
        {
            @Override
            public int compare(TaskConsumption o1, TaskConsumption o2)
            {
                if (o1.getConsumption() > o2.getConsumption())
                {
                    return -1;
                }
                else if (o1.getConsumption() < o2.getConsumption())
                {
                    return 1;
                }
                else
                {
                    if (o1.getTaskId() < o1.getTaskId())
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            }
        };
        Collections.sort(taskList, taskCpr);
        // 若最大负载的任务的负载都大于阈值+平均值了，说明没法最佳迁移
        if (taskList.get(0).getConsumption() > aveCon + threshold)
        {
            return ReturnCodeKeys.E014;
        }
        for (NodeInfo node : nodeInfoList)
        {
            node.setAmount(0);
            node.setTotalConsumption(0);
        }
        // 二重循环，外层遍历taskList，内存遍历nodeListInfo(若总负载+当前任务负载大于平均值了就跳过,否则就塞进去)
        int taskindex = -1;
        int maxLoadNodeId = nodeInfoList.get(nodeInfoList.size() - 1).getNodeId();
        for (int i = 0; i < taskList.size(); i++)
        {
            int con = taskList.get(i).getConsumption();
            for (NodeInfo node : nodeInfoList)
            {
                int nodeId = node.getNodeId();
                int totalConsumption = node.getTotalConsumption();
                int amount = node.getAmount();
                // 若已有总负载和当前任务负载大于平均值就跳过
                if (node.getTotalConsumption() + con > aveCon)
                {
                    if (maxLoadNodeId == node.getNodeId())
                    {
                        taskindex = i;
                    }
                    continue;
                }
                // 如果没超过，就塞给当前节点
                taskList.get(i).setNodeId(nodeId);
                totalConsumption = totalConsumption + con;
                amount++;
                node.setTotalConsumption(totalConsumption);
                node.setAmount(amount);
                break;
            }
            if (taskindex != -1)
            {
                break;
            }
        }
        // 之后较小的负载，那是要分配一个就要对节点负载做排序
        Comparator<NodeInfo> nodecpr = new Comparator<NodeInfo>()
        {
            @Override
            public int compare(NodeInfo o1, NodeInfo o2)
            {
                if (o1.getTotalConsumption() < o2.getTotalConsumption())
                {
                    return -1;
                }
                else if (o1.getTotalConsumption() > o2.getTotalConsumption())
                {
                    return 1;
                }
                else
                {
                    if (o1.getNodeId() < o2.getNodeId())
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            }
        };
        Collections.sort(nodeInfoList, nodecpr);
        if (taskindex != -1)
        {
            for (int i = taskindex; i < taskList.size(); i++)
            {
                NodeInfo node = nodeInfoList.get(0);
                int nodeId = node.getNodeId();
                int totalConsumption = node.getTotalConsumption();
                int amount = node.getAmount();
                taskList.get(i).setNodeId(nodeId);
                totalConsumption = totalConsumption + taskList.get(i).getConsumption();
                amount++;
                node.setTotalConsumption(totalConsumption);
                node.setAmount(amount);
                Collections.sort(nodeInfoList, nodecpr);
            }
        }
        if(nodeInfoList.get(0).getTotalConsumption()-nodeInfoList.get(nodeInfoList.size()-1).getTotalConsumption()<threshold)
        {
            return ReturnCodeKeys.E013;
        }
        else 
        {
            return ReturnCodeKeys.E014;
        }
    }


    public int queryTaskStatus(List<TaskInfo> tasks) {
        if (tasks == null)
        {
            return ReturnCodeKeys.E016;
        }
        for (TaskConsumption task : taskList)
        {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setNodeId(task.getNodeId());
            taskInfo.setTaskId(task.getTaskId());
            tasks.add(taskInfo);
        }
        // 按taskId升序
        Comparator<TaskInfo> taskInfoCpr = new Comparator<TaskInfo>()
        {
            @Override
            public int compare(TaskInfo o1, TaskInfo o2)
            {
                if (o1.getTaskId() < o2.getTaskId())
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
            }
        };
        Collections.sort(tasks, taskInfoCpr);
        return ReturnCodeKeys.E015;
    }

}
