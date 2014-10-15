package org.jed2k.alert;

public abstract class Alert {
    public static enum Severity {
        Debug, 
        Info, 
        Warning, 
        Critical, 
        Fatal, 
        None
    }
    
    public static enum Category {
        ErrorNotification(0x1),
        PeerNotification(0x2),
        PortMappingNotification(0x4),
        StorageNotification(0x8),
        TrackerNotification(0x10),
        DebugNotification(0x20),
        StatusNotification(0x40),
        ProgressNotification(0x80),
        IpBlockNotification(0x100),
        PerformanceWarning(0x200),
        ServerNotification(0x400),
        StatsNotification(0x800),
        AllCategories(0xffffffff);

        public final int value;
        private Category(int value) {
            this.value = value;
        }
    }
    
    public abstract Severity severity();
    public abstract int category();    
}
