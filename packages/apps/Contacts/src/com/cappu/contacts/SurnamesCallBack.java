package com.cappu.contacts;

import com.cappu.contacts.util.FilterNodes;

public interface SurnamesCallBack{
    void updateFilter(FilterNodes filter);
    void showSurGrid(FilterNodes filter ,boolean add);
}
