#!/usr/bin/env python
# coding: utf-8

# In[ ]:


import pandas as pd
from pandas import json_normalize
import matplotlib.pyplot as plt
import os
from collections import Counter
import numpy as np
import shutil
from datetime import datetime
import time
import sys
import json
from pandas.io.json import json_normalize
pd.options.mode.chained_assignment = None

# Set the file directory and read the .csv file as dataframe

# dir = r'C:/Users/USER_20210715/Documents/Data/Delonghi/json/'
# fl = 'NCM2042I01035_202120.json'
# dir_fl = dir + fl
#
# rd_json = open(dir_fl)
# json_data = json.load(rd_json)
def calculateWarmUpTime(json_data):
    #df_org = json_normalize(json_data['data'])
    df_org = json_normalize(json_data)

    # dir = r'C:/Users/USER_20210715/Documents/Data/Delonghi/'
    # # fl = 'NCM2025I01006.csv'
    # fl = 'NCM2025I01023.csv'
    # dir_fl = dir + fl
    # df_org = pd.read_csv(dir_fl)

    # Change the RT column into date format
    # Select necessary columns and sort by dates
    # Only select dates from 2021 and above
    df = df_org[['CI', 'CT', 'RT', 'TAV', 'CTT', 'LST', 'TEMP', 'TFF']]
    df['RT_time'] = df['RT'].apply(lambda x: pd.to_datetime(str(x)))
    df = df.sort_values(by=['RT_time'])
    mask = (df['RT_time'] > '2021-01-01 00:00:00')
    df = df.loc[mask]


    # Generate rt_ls, a list of unique month and day
    # Select the date of interest
    # Select the day before the day of interest
    df['Mon_day'] = df['RT_time'].dt.strftime('%y-%m-%d')
    rt_ls = df['Mon_day'].unique()
    df_final = pd.DataFrame(columns= ['index_col', 'risk', 'shot_count', 'time', 'date'])
    df_pattern = pd.DataFrame(columns= ['CI', 'date', 'Lev_Diff', 'time','TAV','abnorm_ctt', 'Diff_Temp', 'Normal count', 'shot_count', 'Displayed pattern', 'Displayed risk', 'risk'])

    #================================================================================
    temp_mold = 200 
    temp = df['TAV']
    temp_max = temp.max()
    temp_min = temp.min()

    if temp_min > temp_mold :
        temp_min = temp_mold
    if temp_max < temp_mold :
        temp_max = temp_mold


    std_range = 50
    temp_diff = temp_max-temp_min
    #std_temp = (temp_max-temp_min)/23
    std_temp = 10
    mid_temp = (temp_max+temp_min)/2
    df_tmp_17 = []
    df_tmp_24 = []
    df_tmp_22 = []
    df_tmp_23 = []
    df_stab_temp = []
    # In fact, "df_stable_temp = 0" is correct,
    # In abnormal detect algorithm, use the average temperature of "Normal production"
    # and if the "normal production" is not in the data group of day, 
    #       abnormal detect algorithm uses the prevous day's average temperature.
    # but in this code, first day in the original data has not previous day and it's temperature
    # Therefore, in order to evaluate the abnormal detect in the case without the"Nomal productoin",
    #            just set the "df_stab_temp = [480]"
    # In fact, in this case, apply the backdata fuction.
    # only this case, abnormal detect algorithm uses the next day's average temperature of "Normal production"

    # STEP 1. : Data selection

    # len(rt_ls) is the lentgh of date list in the original data
    re_data = []

    for i in range(len(rt_ls)):

        # rt_day is indicated index of "rt_ls"
        rt_day = i

        # sel_day is indecated the value of the i th rt_ls : export the i th date
        sel_day = rt_ls[rt_day]

        # select the value corresponding sel_day in "Mon_day" colume of df frame
        # Assign the seletd day to mask
        mask = (df['Mon_day'] == sel_day)

        # Assign the row of mask in the "df_tmp"    
        df_tmp = df.loc[mask]

        # sel_day's year, month and day is assigned to each item 
        sel_day = datetime.strptime(sel_day, '%y-%m-%d')
        sel_year = sel_day.year
        sel_month = sel_day.month
        sel_days = sel_day.day

        # the data before sel_days (sel_days-1) is assigned to bf_ day
        # Month, Year of sel_days is assigned to bf_year and bf_month
        bf_year = sel_year
        bf_month = sel_month
        bf_day = sel_days - 1

        ft_year = sel_year
        ft_month = sel_month
        ft_day = sel_days +1


        # If the bf_day is zero, the date is 0 and this is not in rearlity.
        # So, the date, month, year data are changed to below.
        if bf_day == 0 :
            if sel_month ==5 or 7 or 8 or 10 or 12 :
                bf_day = 30
                bf_month = sel_month-1
                bf_year = sel_year
                if sel_month == 3 and 4%sel_yaer == 0:
                    bf_day == 29  # leep year
                if sel_month ==3 and 4%sel_yaer != 0:
                    bf_day == 28
                if sel_month == 1:
                    bf_month = 12
                    bf_year = sel_year-1
            if sel_month == 2 or 4 or 6 or 9 or 11 :
                bf_day = 31
                bf_month = sel_month-1

        # if sel_month is less than 10, digit of sel_month is one.
        # sel_month is assinged to format "0+sel_month" <-- It has the two digit for month.
        if bf_month < 10:
            bf_month == '0' + str(bf_month)
            bf_date_str = str(bf_year)[2:4] + '-0' + str(bf_month)
        else :
            bf_date_str = str(bf_year)[2:4] +'-'+str(bf_month)

        # if sel_day is less than 10, digit of sel_month is one.
        # sel_month is assinged to format "0+sel_month" <-- It has the two digit for month.   
        if bf_day < 10 :
            bf_day == '0' + str(bf_day)    
            bf_date_str =  bf_date_str + '-0' + str(bf_day)
        else :
            bf_date_str =bf_date_str + '-' + str(bf_day)
        # bf_date_str is indicated the date before the sel_day (format is "str") 


        if ft_day == 0 :
            if sel_month ==5 or 7 or 8 or 10 or 12 :
                ft_day = 30
                ft_month = sel_month-1
                ft_year = sel_year
                if sel_month == 3 and 4%sel_yaer == 0:
                    ft_day == 29  # leep year
                if sel_month ==3 and 4%sel_yaer != 0:
                    ft_day == 28
                if sel_month == 1:
                    ft_month = 12
                    ft_year = sel_year-1
            if sel_month == 2 or 4 or 6 or 9 or 11 :
                ft_day = 31
                ft_month = sel_month-1

        # if sel_month is less than 10, digit of sel_month is one.
        # sel_month is assinged to format "0+sel_month" <-- It has the two digit for month.
        if ft_month < 10:
            ft_month == '0' + str(ft_month)
            ft_date_str = str(ft_year)[2:4] + '-0' + str(ft_month)
        else :
            ft_date_str = str(ft_year)[2:4] +'-'+str(ft_month)

        # if sel_day is less than 10, digit of sel_month is one.
        # sel_month is assinged to format "0+sel_month" <-- It has the two digit for month.   
        if ft_day < 10 :
            ft_day == '0' + str(ft_day)    
            ft_date_str =  ft_date_str + '-0' + str(ft_day)
        else :
            ft_date_str =ft_date_str + '-' + str(ft_day)
        # bf_date_str is indicated the date before the sel_day (format is "str") 


        # If the date before the selected day exist in the df frame,
        # Select the last entry from the previous day to the current day
        if bf_date_str in rt_ls:
            bf_day = rt_ls[rt_day-1] 
            mask_bf = (df['Mon_day'] == bf_day)
            df_tmp_bf=df.loc[mask_bf]
            delete_bf_day = bf_day
            df_tmp = pd.concat([df_tmp, df_tmp_bf])
            df_tmp = df_tmp.sort_values(by=['RT_time'])  # <-- the data group of sel_day is complete in order to calculae the change of temperature


        # If the bf_date_str is not in "rt_ls", just the data group of sel_day is olny consist of the data of sel_day. It is not included the data of previous day.
        else :
            sel_day = rt_ls[rt_day] 
            df_tmp =  df.loc[(df['Mon_day'] == sel_day)]   # <-- the data group of sel_day is complete in order to calculae the change of temperature


        if ft_date_str in rt_ls:

            ft_day = rt_ls[rt_day+1] 
            mask_ft = (df['Mon_day'] == ft_day)
            df_tmp_ft=df.loc[mask_ft]
            delete_ft_day = ft_day
            df_tmp = pd.concat([df_tmp, df_tmp_ft])
            df_tmp = df_tmp.sort_values(by=['RT_time'])  # <-- the data group of sel_day is complete in order to calculae the change of temperature


        # If the bf_date_str is not in "rt_ls", just the data group of sel_day is olny consist of the data of sel_day. It is not included the data of previous day.
        else :
            sel_day = rt_ls[rt_day] 
            df_tmp =  df.loc[(df['Mon_day'] == sel_day)]  


        # the change of temperature is not calcuated in first low of the data group.
        # Therefore, after calculation of the change of temperature, the first row of the data group will be deleted because the change of temperature and abs data is not assigned.         





        ls_drp = ['TFF', 'TEMP', 'LST']
      #==============================================================
        df_s1 = df_tmp.drop(ls_drp, axis=1)
        df_s1 = df_tmp.drop(ls_drp, axis=1)
        df_s1['Diff_Temp'] = df_s1['TAV'].diff()

        index_col=[]

        # Hour data of "RT" is assigned to index_col
        for j in range (0, len(df_s1), 1) :
            RT_data_raw = str(df_s1.iloc[j]['RT'])
            RT_index = int(RT_data_raw[8:10])
            index_col.append(RT_index)
        # Assign the "index_col" to "df_s1" as the "index_col"
        df_s1['index_col']=index_col

        df_s1 = df_s1.sort_values(by=['RT_time'])
        df_s1['Diff_Temp'].iloc[0] = (df_s1['Diff_Temp'].iloc[1])





        det_sect = []
        df_diff_inx = []
        df_sect = []
        df_TAV = []
        gr_sect = []
        gr_TAV = []
        df_CT = []
        gr_CT = []
        df_diff = []
        gr_diff = []





        for j in range (0, len(df_s1), 1) :
            df_diff_temp = df_s1['Diff_Temp']

            df_diff_temp = df_diff_temp.iloc[j]



            if abs(df_diff_temp) < std_temp :

                df_diff_inx.append(0)

            # differnce of tmpearture > 0 -> deviaton(gradient) > 0, positice change : Increase
            elif df_diff_temp > 0 and abs(df_diff_temp) >= std_temp :

                df_diff_inx.append(1)

            # differnce of tmpearture < 0 -> deviaton(gradient) < 0, negative change : Decrease
            if df_diff_temp < 0 and abs(df_diff_temp) >= std_temp :

                df_diff_inx.append(2)
        df_s1['Lev_Diff'] = df_diff_inx
        tmp_9 = 0
        tmp_10 = len(df_diff_inx)
        tmp_13 = 0
        df_tmp_9 = []


        # Seprate the change of temperature along to "df_diff_inx"
        # Section "0" : stable, Section "1" : Increase, Sctoin "2" : Decrease

        for j in range (0, len(df_diff_inx), 1):
            cr_df_diff_inx = df_diff_inx[j]
            tmp_TAV = df_s1['TAV'].iloc[j]
            tmp_CT = df_s1['CT'].iloc[j]
            tmp_diff = df_s1['Diff_Temp'].iloc[j]

            # if i = 0, assign the value to dataframe
            if j == 0 :
                df_sect.append(cr_df_diff_inx)
                df_TAV.append(tmp_TAV)
                df_CT.append(tmp_CT)
                df_diff.append(tmp_diff)

            if j >= 1 :
                # Compare the currnet "df_diff_inx" and before "df_diff_inx"
                # If before and current value is same, assign the value to dataframe
                bf_df_diff_inx = df_diff_inx[j-1]
                if cr_df_diff_inx == bf_df_diff_inx :
                    df_sect.append(cr_df_diff_inx)
                    df_TAV.append(tmp_TAV)
                    df_CT.append(tmp_CT)
                    df_diff.append(tmp_diff)

                # If before and current valud is not same,
                # the section before is end and the new sectioin is start with current value.
                if cr_df_diff_inx != bf_df_diff_inx :

                    # assign the end of the section to group of section and TAV
                    gr_sect.append(df_sect)
                    gr_TAV.append(df_TAV)
                    gr_CT.append(df_CT)
                    gr_diff.append(df_diff)

                    # reset the sectoin value
                    df_sect = []
                    df_TAV = []
                    df_CT = []
                    df_diff = []
                    # assign the new value of section with start
                    df_sect.append(cr_df_diff_inx)
                    df_TAV.append(tmp_TAV)
                    df_CT.append(tmp_CT)
                    df_diff.append(tmp_diff)

            if j == len(df_diff_inx)-1 :
                    gr_sect.append(df_sect)
                    gr_TAV.append(df_TAV)
                    gr_CT.append(df_CT)
                    gr_diff.append(df_diff)


        max_TAV = df_s1['TAV'].max()
        min_TAV = df_s1['TAV'].min()
        diff_max_min_TAV = max_TAV-min_TAV
        df_sect_inx = []
        df_s1 = df_s1.sort_values(by=['RT_time'])
        for j in range (0, len(gr_sect), 1):
            tmp_sect = gr_sect[j]

            # the value of "tmp_gr_sect" is assinged to "val_gr_sect"
            # valuses in tmp_gr_sect is the same becuase the same state
            val_sect = tmp_sect[0]

            length_sect = len(tmp_sect)


            # length of data group is 24 and stable change of temperature is 100
            # Therefore, stable chagne per hour is indicated by "grad_stab"
            grad_stab = std_temp

            # "stand_sect" = stable change per hour * total hours of section
            #              = chagne of temperature assumed that section is stable
            # To compare with section data, set the standard 
            stand_sect = grad_stab*(len(tmp_sect))

            # calculate the real change of temperature in the sectoin


            TAV_max_sect = max(gr_TAV[j])
            TAV_min_sect = min(gr_TAV[j])
            TAV_mean_sect = sum(gr_TAV[j])/len(gr_TAV[j])
            TAV_max_min_sect = abs(sum(gr_diff[j]))

            No_pro = 0
            gr_val_CT = gr_CT[j]
            for k in range (len(gr_val_CT)):
                val_CT = gr_val_CT[k]
                if val_CT == 0:
                    No_pro = No_pro+1
            per_No_pro = No_pro/len(gr_val_CT)
            # if the value of sectoin is "1", the section is "Increase"
            if val_sect == 1 :
                # If change of temperature is bigger than standard stable chagne,
                # the secton is "Warm-up"
                # "2" is indicated the "warm-up"
                if TAV_max_min_sect >= stand_sect :
                    for j in range (length_sect) :
                        df_sect_inx.append(2)

                # If change of temperature is lower than standard stable chagne,
                # the secton is "stable"        
                if TAV_max_min_sect < stand_sect :


                    # if average temperature is lower than 350,
                    # the section is "No production"
                    if per_No_pro >= 0.8 :
                        for j in range (length_sect) :
                            df_sect_inx.append(1)

                    # if average temperatuer is bigger than 350
                    # if the average cycle time CT was recorded 0 in the sectoin at lest once
                    # in this case, section is "Risk production"
                    # because although the sectoin is stable and temperature is bigger than 350,
                    # no production hour exists in the sectoin.
                    if per_No_pro < 0.8:
                        for j in range (length_sect) :
                            df_sect_inx.append(0)
                    # In this case, the section is "Normal production"      


            # if the value of sectoin is "2", the section is "Decrease"
            if val_sect == 2 :
                if TAV_max_min_sect >= stand_sect :
                    for j in range (length_sect) :
                        df_sect_inx.append(3)
                if TAV_max_min_sect < stand_sect :

                    if per_No_pro >= 0.8 :
                        for j in range (length_sect) :
                            df_sect_inx.append(1)
                    if per_No_pro < 0.8 :
                        for j in range (length_sect) :
                            df_sect_inx.append(0)


            if val_sect == 0 :

                if per_No_pro >= 0.8 :
                    for j in range (length_sect) :
                        df_sect_inx.append(1)
                if per_No_pro < 0.8 :
                    for j in range (length_sect) :
                        df_sect_inx.append(0)   


        df_s1['Pattern index']=df_sect_inx   


        if bf_date_str in rt_ls:
            df_s1 = df_s1[df_s1.Mon_day != delete_bf_day]

        if ft_date_str in rt_ls:
            df_s1 = df_s1[df_s1.Mon_day != delete_ft_day]



        tmp_mask = (df_s1['Pattern index'] == 0)
        df_normal = df_s1.loc[tmp_mask]
        df_TAV_normal = df_normal['TAV']
        val_stab_temp = df_TAV_normal.mean()

        # Becuse of the case that the data group is not ncluded the "Normal production"
        # If "Normal productoin" is not in the data group, "tmp_TAV_normal" is return "NaN"
        if val_stab_temp < 1 :
            val_stab_temp = 0

        # with including the currnet stable temperature, 
        # latest stable tempeature is assigned to "val_stab_temp"
        df_detect_temp = []
        df_TAV_detect=df_s1['TAV']
        for j in range (len(df_s1)) :
            tmp_TAV_detect = df_TAV_detect.iloc[j]

            # If temperature is in the ragne of stable temperature +-50,
            # data is normal and "1" is indicated to "normal"
            if tmp_TAV_detect >= val_stab_temp-std_range and tmp_TAV_detect <= val_stab_temp+std_range :
                df_detect_temp.append(1)

            # If temperature is not in the ragne of stable temperature +-50,
            # data is abnormal and "0" is indicated to "abnomral"
            # in this step, just uses the temperature data in order to detect abnormal
            else :
                df_detect_temp.append(0)
        df_s1['Abnormal_Temp'] = df_detect_temp

        # To use the cycle time data to detect the abnormal, 
        # assign the cycle time data to "ctt_ls"
        ctt_ls = df_s1['CTT'].to_list()
        ctt_total_cnt = []
        dict_ls = []

        # our server, cylce time is recorded by "cycle time / frequency"
        # So, separates cycle time data from "cycle time / frequency"""
        for j in range(len(ctt_ls)):
            tmp = ctt_ls[j].replace('/', " ")
            tmp = tmp.split()
            len_tmp = len(tmp)
            dict = {}
            for k in range(len_tmp):
                if (k % 2) == 0:
                    key = int(tmp[k])
                    val = int(tmp[k +1])
                    dict[key] = val
            ct_cnt = sum(map(int, dict.values()))
            ctt_total_cnt.append(ct_cnt)
            dict_ls.append(dict)
            ct_mal_cnt = []

            # calculate the mode cycle time(most frequency)
            # set the normal range : mode cycle time +-10%
            for j in dict_ls:
                abnorm_dict = {}
                if bool(j) is not False:
                    ct_mode = max(j, key=j.get)
                    ct_max = ct_mode * 1.10
                    ct_min = ct_mode - ct_mode * 0.10
                    for k in j.keys():
                        if k > ct_max or k < ct_min:
                            abnorm_dict[k] = j[k]
                    abnorm_cnt = sum(abnorm_dict.values())
                    if abnorm_cnt == 0:
                        ct_mal_cnt.append(0)
                    else:
                        ct_mal_cnt.append(abnorm_cnt)
                else:
                    ct_mode = 0
                    ct_mal_cnt.append(0)

        df_s1['Abnormal_Temp'] = df_detect_temp
        df_s1['abnorm_ctt'] = ct_mal_cnt
        df_s1['shot_count'] = ctt_total_cnt
        df_abnormal = []

        for j in range((len(df_detect_temp))):
            abnormal_sect = df_sect_inx[j]
            abnormal_temp = df_detect_temp[j]
            abnormal_cycle = ct_mal_cnt[j]
            Total_count = ctt_total_cnt[j]
            if abnormal_temp == 0:
                df_abnormal.append(Total_count)
            elif abnormal_temp == 1 and abnormal_sect == 2 :
                df_abnormal.append(abnormal_cycle)
            elif abnormal_temp == 1 and abnormal_sect == 3 :
                df_abnormal.append(abnormal_cycle)
            else :
                df_abnormal.append(abnormal_cycle)
        df_s1['abnorm_ctt'] = df_abnormal

        df_normal_count = []

        # calculate the normal count
        for j in range(len(df_s1)) :
            tmp_abnormal = df_s1['abnorm_ctt'].iloc[j]
            tmp_total = df_s1['shot_count'].iloc[j]
            tmp_normal = tmp_total-tmp_abnormal
            df_normal_count.append(tmp_normal)
        df_s1['Normal count'] = df_normal_count



        #=======================================
        # Step 6 : : Calculate the abnormal shot      
        #=======================================




        # plot code for abnormal detect algorithm

        df_s1['time'] = pd.to_datetime(df['RT_time']).dt.strftime("%H")
        df_s1['date'] = pd.to_datetime(df['RT_time']).dt.strftime("%Y%m%d")
        df_pattern = df_pattern.append(df_s1)
        

    df_cal = []
    gr_tmp_cal = []
    for j in range (0, len(df_pattern), 1):
        tmp_cal = df_pattern['Pattern index']
        cr_tmp_cal = tmp_cal.iloc[j]
        if j == 0 :
            df_cal.append(cr_tmp_cal)
        if j >= 1 :
            # Compare the currnet "df_diff_inx" and before "df_diff_inx"
            # If before and current value is same, assign the value to dataframe
            bf_tmp_cal = tmp_cal.iloc[j-1]
            if cr_tmp_cal == bf_tmp_cal :
                df_cal.append(cr_tmp_cal)

                # If before and current valud is not same,
            # the section before is end and the new sectioin is start with current value.
            if cr_tmp_cal != bf_tmp_cal :
                # assign the end of the section to group of section and TAV
                gr_tmp_cal.append(df_cal)
                # reset the sectoin value
                df_cal = []
                # assign the new value of section with start
                df_cal.append(cr_tmp_cal)

        if j == len(df_pattern)-1 :
            gr_tmp_cal.append(df_cal)

    det = []     
    for j in range(0, len(gr_tmp_cal), 1) :
        if j == 0 :
            cr = gr_tmp_cal[j]
            for k in range (len(cr)) :
                ccr = cr[k]
                det.append(ccr)
        elif j >= 1 and j < len(gr_tmp_cal)-1 :     
            br = ab
            cr = gr_tmp_cal[j]
            ft = gr_tmp_cal[j+1]
            if len(cr) == 1:
                if br != cr[0] and cr[0] != ft[0] :
                    for k in range (len(cr)) :
                        det.append(ft[0])
                else :
                    for k in range (len(cr)) :
                        ccr = cr[k]
                        det.append(ccr)  
            else :
                for k in range (len(cr)) :
                    ccr = cr[k]
                    det.append(ccr)        

        elif j == len(gr_tmp_cal)-1 :
            cr = gr_tmp_cal[j]
            for k in range (len(cr)) :
                ccr = cr[k]
                det.append(ccr)
        ab = det[len(det)-1]

    df_pattern['Pattern index'] = det




    df_cal = []
    gr_tmp_cal = []
    df_tcal = []
    gr_tcal = []
    for j in range (0, len(df_pattern), 1):
        tmp_cal = df_pattern['Pattern index']
        cr_tmp_cal = tmp_cal.iloc[j]
        tmp_tp = df_pattern['TAV']
        cr_tmp_tp = tmp_tp.iloc[j]
        if j == 0 :
            df_cal.append(cr_tmp_cal)
            df_tcal.append(cr_tmp_tp)
        if j >= 1 :
            # Compare the currnet "df_diff_inx" and before "df_diff_inx"
            # If before and current value is same, assign the value to dataframe
            bf_tmp_cal = tmp_cal.iloc[j-1]
            if cr_tmp_cal == bf_tmp_cal :
                df_cal.append(cr_tmp_cal)
                df_tcal.append(cr_tmp_tp)

                # If before and current valud is not same,
            # the section before is end and the new sectioin is start with current value.
            if cr_tmp_cal != bf_tmp_cal :
                # assign the end of the section to group of section and TAV
                gr_tmp_cal.append(df_cal)
                gr_tcal.append(df_tcal)
                # reset the sectoin value
                df_cal = []
                df_tcal= []
                # assign the new value of section with start
                df_cal.append(cr_tmp_cal)
                df_tcal.append(cr_tmp_tp)

        if j == len(df_pattern)-1 :
            gr_tmp_cal.append(df_cal)
            gr_tcal.append(df_tcal)




    det = []   
    # Normal production 사이에 값이 있는 경우에는 Normal production으로 변환           

    for j in range(0, len(gr_tmp_cal), 1) :
        if j == 0 :
            cr = gr_tmp_cal[j]
            for k in range (len(cr)) :
                ccr = cr[k]
                det.append(ccr)
        elif j >= 1 and j < len(gr_tmp_cal)-1 :     
            br = gr_tmp_cal[j-1]
            cr = gr_tmp_cal[j]
            ft = gr_tmp_cal[j+1]
            if br[0] == 0 and cr[0] != 0 and ft[0] ==0 and len(br) > 1 and len(cr) > 1 :
                for k in range (len(cr)) :
                    det.append(0)

            else :
                cr = gr_tmp_cal[j]
                for k in range (len(cr)) :
                    ccr = cr[k]
                    det.append(ccr)        

        elif j == len(gr_tmp_cal)-1 :
            cr = gr_tmp_cal[j]
            for k in range (len(cr)) :
                ccr = cr[k]
                det.append(ccr) 

    df_pattern['Pattern index'] = det








    df_final_pattern = []
    df_tmp_risk = []


    for j in range (len(df_pattern)) :
        df_tmp_abnormal = df_pattern['abnorm_ctt']
        df_tmp_total = df_pattern['shot_count']
        tmp_pattern = df_pattern['Pattern index'].iloc[j]
        tmp_ab_count = df_tmp_abnormal.iloc[j]
        tmp_total_count = df_tmp_total.iloc[j]


        if tmp_pattern == 1 and tmp_total_count != 0 :
            df_final_pattern.append(1)
            df_tmp_risk.append(2)

        elif tmp_pattern == 0 and tmp_ab_count > 0 and tmp_total_count !=0:
            df_final_pattern.append(0)
            df_tmp_risk.append(1)


        elif tmp_pattern == 0 and tmp_ab_count == 0 and tmp_total_count != 0:
            df_final_pattern.append(0)
            df_tmp_risk.append(0)

        elif tmp_pattern == 0 and tmp_total_count == 0:
            df_final_pattern.append(1)
            df_tmp_risk.append(3)



        elif tmp_pattern == 1 :
            df_final_pattern.append(1)
            df_tmp_risk.append(3)


        elif tmp_pattern == 2 and tmp_total_count !=0:
            df_final_pattern.append(2)
            df_tmp_risk.append(2)
            
        elif tmp_pattern == 2 and tmp_total_count == 0:
            df_final_pattern.append(1)
            df_tmp_risk.append(3)            
            


        elif tmp_pattern == 3 and tmp_total_count != 0:
            df_final_pattern.append(3)
            df_tmp_risk.append(2)

        elif tmp_pattern == 3 and tmp_total_count == 0:
            df_final_pattern.append(1)
            df_tmp_risk.append(3)            



    df_pattern['det_sect'] = df_final_pattern
    df_pattern['Risk_ind'] = df_tmp_risk    














    df_dis_pattern = []
    df_dis_risk =[]

    for j in range (len(df_pattern)) :
        dis_pattern = df_pattern['det_sect'].iloc[j]
        if dis_pattern == 0 :
            df_dis_pattern.append('Normal production')
        if dis_pattern == 1 :
            df_dis_pattern.append('No production')
        if dis_pattern == 2 :
            df_dis_pattern.append('Warm-up')
        if dis_pattern == 3 :
            df_dis_pattern.append('Cool-down')  


    for j in range (len(df_pattern)) :       
        dis_risk = df_pattern['Risk_ind'].iloc[j]
        if dis_risk == 0 :
            df_dis_risk.append('low')
        if dis_risk == 1 :
            df_dis_risk.append('medium')
        if dis_risk == 2 :
            df_dis_risk.append('high')
        if dis_risk == 3 :
            df_dis_risk.append('blank')  



    df_pattern['Displayed pattern'] = df_dis_pattern
    df_pattern['risk'] = df_dis_risk
    #========================================================
    df_final = df_pattern     
    df_final = df_final[["date", "time", "CI", "shot_count", "risk", "det_sect", "abnorm_ctt"]]
    df_final = df_final.sort_values(['date', 'time'], ascending=[True, True])
    result = df_final.to_json(orient="records")
    print(result)
    return json.loads(result)
    
    
    

