package com.elytraforce.bungeesuite.antiswear;

public interface Filter {


    Boolean filter(String string);

    FilterPriority getPriority();
    
    Integer getVls();

    default String getName() {
        return this.getClass().getName();
    }

    enum FilterPriority {

        LOW, LOWEST, NORMAL, HIGH, HIGHEST, MONITOR

    }

}