package com.ericsson.asr.poc.cachetest.types;

import java.util.Arrays;

public class CTUMEvent {
    int BYTE_HOUR = 1;
    int BYTE_MINUTE = 2;
    int BYTE_SECOND = 3;
    int TIME_MILLISECOND = 124;
    int MACRO_ENODEB_ID = 23;
    int HOME_ENODEB_ID = 44444;
    byte[] IMSI = { 1, 2, 3, 4, 5, 6, 7, 8 };
    byte[] IMEISV = { 1, 2, 3, 4, 5, 6, 7, 8 };
    byte[] PLMN_IDENTITY = { 1, 2, 3 };
    int MMEGI = 6666;
    int MMEC = 345345;
    int MME_UE_S1AP_ID = 14324;
    int ENB_UE_S1AP_ID = 34436;

    public int getBYTE_HOUR() {
        return BYTE_HOUR;
    }

    public void setBYTE_HOUR(int bYTE_HOUR) {
        BYTE_HOUR = bYTE_HOUR;
    }

    public int getBYTE_MINUTE() {
        return BYTE_MINUTE;
    }

    public void setBYTE_MINUTE(int bYTE_MINUTE) {
        BYTE_MINUTE = bYTE_MINUTE;
    }

    public int getBYTE_SECOND() {
        return BYTE_SECOND;
    }

    public void setBYTE_SECOND(int bYTE_SECOND) {
        BYTE_SECOND = bYTE_SECOND;
    }

    public int getTIME_MILLISECOND() {
        return TIME_MILLISECOND;
    }

    public void setTIME_MILLISECOND(int tIME_MILLISECOND) {
        TIME_MILLISECOND = tIME_MILLISECOND;
    }

    public int getMACRO_ENODEB_ID() {
        return MACRO_ENODEB_ID;
    }

    public void setMACRO_ENODEB_ID(int mACRO_ENODEB_ID) {
        MACRO_ENODEB_ID = mACRO_ENODEB_ID;
    }

    public int getHOME_ENODEB_ID() {
        return HOME_ENODEB_ID;
    }

    public void setHOME_ENODEB_ID(int hOME_ENODEB_ID) {
        HOME_ENODEB_ID = hOME_ENODEB_ID;
    }

    public byte[] getIMSI() {
        return IMSI;
    }

    public void setIMSI(byte[] iMSI) {
        IMSI = iMSI;
    }

    public byte[] getIMEISV() {
        return IMEISV;
    }

    public void setIMEISV(byte[] iMEISV) {
        IMEISV = iMEISV;
    }

    public byte[] getPLMN_IDENTITY() {
        return PLMN_IDENTITY;
    }

    public void setPLMN_IDENTITY(byte[] pLMN_IDENTITY) {
        PLMN_IDENTITY = pLMN_IDENTITY;
    }

    public int getMMEGI() {
        return MMEGI;
    }

    public void setMMEGI(int mMEGI) {
        MMEGI = mMEGI;
    }

    public int getMMEC() {
        return MMEC;
    }

    public void setMMEC(int mMEC) {
        MMEC = mMEC;
    }

    public int getMME_UE_S1AP_ID() {
        return MME_UE_S1AP_ID;
    }

    public void setMME_UE_S1AP_ID(int mME_UE_S1AP_ID) {
        MME_UE_S1AP_ID = mME_UE_S1AP_ID;
    }

    public int getENB_UE_S1AP_ID() {
        return ENB_UE_S1AP_ID;
    }

    public void setENB_UE_S1AP_ID(int eNB_UE_S1AP_ID) {
        ENB_UE_S1AP_ID = eNB_UE_S1AP_ID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + BYTE_HOUR;
        result = prime * result + BYTE_MINUTE;
        result = prime * result + BYTE_SECOND;
        result = prime * result + ENB_UE_S1AP_ID;
        result = prime * result + HOME_ENODEB_ID;
        result = prime * result + Arrays.hashCode(IMEISV);
        result = prime * result + Arrays.hashCode(IMSI);
        result = prime * result + MACRO_ENODEB_ID;
        result = prime * result + MMEC;
        result = prime * result + MMEGI;
        result = prime * result + MME_UE_S1AP_ID;
        result = prime * result + Arrays.hashCode(PLMN_IDENTITY);
        result = prime * result + TIME_MILLISECOND;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CTUMEvent other = (CTUMEvent) obj;
        if (BYTE_HOUR != other.BYTE_HOUR)
            return false;
        if (BYTE_MINUTE != other.BYTE_MINUTE)
            return false;
        if (BYTE_SECOND != other.BYTE_SECOND)
            return false;
        if (ENB_UE_S1AP_ID != other.ENB_UE_S1AP_ID)
            return false;
        if (HOME_ENODEB_ID != other.HOME_ENODEB_ID)
            return false;
        if (!Arrays.equals(IMEISV, other.IMEISV))
            return false;
        if (!Arrays.equals(IMSI, other.IMSI))
            return false;
        if (MACRO_ENODEB_ID != other.MACRO_ENODEB_ID)
            return false;
        if (MMEC != other.MMEC)
            return false;
        if (MMEGI != other.MMEGI)
            return false;
        if (MME_UE_S1AP_ID != other.MME_UE_S1AP_ID)
            return false;
        if (!Arrays.equals(PLMN_IDENTITY, other.PLMN_IDENTITY))
            return false;
        if (TIME_MILLISECOND != other.TIME_MILLISECOND)
            return false;
        return true;
    }

}
