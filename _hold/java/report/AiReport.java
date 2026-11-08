package com.ruoyi.web.core.report;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟的 AI 全栈开发周报。数字固定，便于核对。
 */
public class AiReport
{
    private String title;

    private String period;

    private List<Card> cards = new ArrayList<>();

    private List<DayStat> daily = new ArrayList<>();

    private List<ModuleStat> modules = new ArrayList<>();

    private List<TaskRow> tasks = new ArrayList<>();

    public static AiReport sample()
    {
        AiReport report = new AiReport();
        report.title = "AI 全栈开发周报";
        report.period = "2026-10-22 至 2026-10-28";
        report.cards.add(new Card("已完成需求", 4, "项"));
        report.cards.add(new Card("进行中", 1, "项"));
        report.cards.add(new Card("代码提交", 27, "次"));
        report.cards.add(new Card("通过的检查", 18, "项"));
        report.daily.add(new DayStat("10-22", 3, 1));
        report.daily.add(new DayStat("10-23", 5, 1));
        report.daily.add(new DayStat("10-24", 2, 0));
        report.daily.add(new DayStat("10-25", 4, 0));
        report.daily.add(new DayStat("10-26", 6, 1));
        report.daily.add(new DayStat("10-27", 4, 0));
        report.daily.add(new DayStat("10-28", 3, 1));
        report.modules.add(new ModuleStat("登录与权限", 100, "已完成"));
        report.modules.add(new ModuleStat("改表", 100, "已完成"));
        report.modules.add(new ModuleStat("开发报表", 100, "已完成"));
        report.modules.add(new ModuleStat("接口联调", 60, "进行中"));
        report.modules.add(new ModuleStat("部署检查", 20, "未开始"));
        report.tasks.add(new TaskRow("接入若依登录", "AI", "已完成", "10-23"));
        report.tasks.add(new TaskRow("上传表格并改 1 到 3 列", "AI", "已完成", "10-26"));
        report.tasks.add(new TaskRow("按后台数据生成周报", "AI", "已完成", "10-28"));
        report.tasks.add(new TaskRow("联调预览和导出", "AI", "进行中", "10-28"));
        report.tasks.add(new TaskRow("核对生产配置", "AI", "未开始", "10-28"));
        return report;
    }

    public String getTitle()
    {
        return title;
    }

    public String getPeriod()
    {
        return period;
    }

    public List<Card> getCards()
    {
        return cards;
    }

    public List<DayStat> getDaily()
    {
        return daily;
    }

    public List<ModuleStat> getModules()
    {
        return modules;
    }

    public List<TaskRow> getTasks()
    {
        return tasks;
    }

    public static class Card
    {
        private String label;

        private int value;

        private String unit;

        public Card()
        {
        }

        public Card(String label, int value, String unit)
        {
            this.label = label;
            this.value = value;
            this.unit = unit;
        }

        public String getLabel()
        {
            return label;
        }

        public int getValue()
        {
            return value;
        }

        public String getUnit()
        {
            return unit;
        }
    }

    public static class DayStat
    {
        private String day;

        private int commits;

        private int features;

        public DayStat()
        {
        }

        public DayStat(String day, int commits, int features)
        {
            this.day = day;
            this.commits = commits;
            this.features = features;
        }

        public String getDay()
        {
            return day;
        }

        public int getCommits()
        {
            return commits;
        }

        public int getFeatures()
        {
            return features;
        }
    }

    public static class ModuleStat
    {
        private String name;

        private int progress;

        private String status;

        public ModuleStat()
        {
        }

        public ModuleStat(String name, int progress, String status)
        {
            this.name = name;
            this.progress = progress;
            this.status = status;
        }

        public String getName()
        {
            return name;
        }

        public int getProgress()
        {
            return progress;
        }

        public String getStatus()
        {
            return status;
        }
    }

    public static class TaskRow
    {
        private String name;

        private String owner;

        private String status;

        private String updatedAt;

        public TaskRow()
        {
        }

        public TaskRow(String name, String owner, String status, String updatedAt)
        {
            this.name = name;
            this.owner = owner;
            this.status = status;
            this.updatedAt = updatedAt;
        }

        public String getName()
        {
            return name;
        }

        public String getOwner()
        {
            return owner;
        }

        public String getStatus()
        {
            return status;
        }

        public String getUpdatedAt()
        {
            return updatedAt;
        }
    }
}
