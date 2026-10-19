package com.luckyzyx.bypasspower;

interface IBypassPowerController {
    boolean checkBypassMode();
    boolean getBypassMode();
    void setBypassMode(boolean status);
}
