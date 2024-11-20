module mscalc.engine {
    exports mscalc.engine.cpp;
    exports mscalc.engine.ratpack;
    exports mscalc.engine;
    exports mscalc.engine.commands;
    exports mscalc.engine.resource;

    requires java.xml;
    requires org.apache.logging.log4j;
}