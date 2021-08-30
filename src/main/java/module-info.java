module mailman {
    requires java.desktop;
    requires TimingFramework;
    requires binding;
    requires swingx;
    requires Filters;
    requires swing.layout;

    exports com.sun.javaone.mailman.ui to binding;
}