﻿using System;
using System.Diagnostics;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;
using System.Windows.Controls;
using System.Windows.Media;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Runtime.InteropServices;
using System.Text;
using System.IO;
using MVVM.Model;
using Misc;
using CssLib;

namespace MVVM.ViewModel
{
    public class IntroViewModel : BaseViewModel
    {
        Intro m_intro = new Intro();
        public ObservableCollection<object> TheViews { get; set; }

        private ConfigViewModelS m_configViewModelS;
        private ConfigViewModelU m_configViewModelU;
        private ConfigViewModelSDest m_configViewModelSDest;
        private ConfigViewModelUDest m_configViewModelUDest;
        private OptionsViewModel m_optionsViewModel;
        private UsersViewModel m_usersViewModel;
        private ScheduleViewModel m_scheduleViewModel;
        private AccountResultsViewModel m_resultsViewModel;
        private Grid hg;
        private Grid vg;
        public CSMigrationwrapper mw;
        public IntroViewModel(ListBox lbMode, Grid helpGrid, Grid viewsGrid)
        {
            lb = lbMode;
            hg = helpGrid;
            vg = viewsGrid;
            this.GetIntroLicenseCommand = new ActionCommand(this.GetIntroLicense, () => true);
            this.GetIntroUserMigCommand = new ActionCommand(this.GetIntroUserMig, () => true);
            this.GetIntroServerMigCommand = new ActionCommand(this.GetIntroServerMig, () => true);
            this.BeginCommand = new ActionCommand(this.Begin, () => true);
            mw = new CssLib.CSMigrationwrapper();
        }

        public UsersViewModel GetUsersViewModel()
        {
            return m_usersViewModel;
        }

        public ICommand GetIntroLicenseCommand
        {
            get;
            private set;
        }

        private void GetIntroLicense()
        {
            string urlString = "http://files.zimbra.com/website/docs/zimbra_network_la.pdf";
            Process.Start(new ProcessStartInfo(urlString));
        }

        public ICommand GetIntroUserMigCommand
        {
            get;
            private set;
        }

        private void GetIntroUserMig()
        {
            BaseViewModel.isServer = false;
        }

        public ICommand GetIntroServerMigCommand
        {
            get;
            private set;
        }

        private void GetIntroServerMig()
        {
            BaseViewModel.isServer = true;
        }

        public ICommand BeginCommand
        {
            get;
            private set;
        }

        private void Begin()
        {
            TheViews.RemoveAt(0);

            // Get data to initialize the profile combo boxes
            mw.MailClient = "MAPI";
            mw.InitializeInterop();
            string[] profiles = mw.GetListofMapiProfiles();

            if (BaseViewModel.isServer)
            {
                BaseViewModel.isServer = true;
                TheViews.Add(m_configViewModelS);
                TheViews.Add(m_configViewModelSDest);
                TheViews.Add(m_optionsViewModel);
                TheViews.Add(m_usersViewModel);
                TheViews.Add(m_scheduleViewModel);
                TheViews.Add(m_resultsViewModel);
                foreach (string s in profiles)
                {
                    m_configViewModelS.ProfileList.Add(s);
                }
                m_optionsViewModel.ImportNextButtonContent = "Next";
            }
            else
            {
                BaseViewModel.isServer = false;
                TheViews.Add(m_configViewModelU);
                TheViews.Add(m_configViewModelUDest);
                TheViews.Add(m_optionsViewModel);
                TheViews.Add(m_resultsViewModel);
                foreach (string s in profiles)
                {
                    m_configViewModelU.ProfileList.Add(s);
                }
                m_optionsViewModel.ImportNextButtonContent = "Migrate";
            }

            hg.Visibility = Visibility.Visible;
            vg.Background = new SolidColorBrush((Color)ColorConverter.ConvertFromString("#E7E7E7"));

            lb.Background = new SolidColorBrush((Color)ColorConverter.ConvertFromString("#E7E7E7"));
            lb.Visibility = Visibility.Visible;
            lb.IsEnabled = true;
            lb.SelectedIndex = 0;
        }

        public string BuildNum
        {
            get { return m_intro.BuildNum; }
            set
            {
                if (value == m_intro.BuildNum)
                {
                    return;
                }
                m_intro.BuildNum = value;

                OnPropertyChanged(new PropertyChangedEventArgs("BuildNum"));
            }
        }

        public string WelcomeMsg
        {
            get { return m_intro.WelcomeMsg; }
            set
            {
                if (value == m_intro.WelcomeMsg)
                {
                    return;
                }
                m_intro.WelcomeMsg = value;

                OnPropertyChanged(new PropertyChangedEventArgs("WelcomeMsg"));
            }
        }

        public string InstallDir
        {
            get;
            set;
        } 

        public void SetupViewModelPtrs()
        {
            for (int i = 0; i < (int)ViewType.MAX; i++)
            {
                ViewModelPtrs[i] = null;
            }

            ViewModelPtrs[(int)ViewType.INTRO] = this;
            ViewModelPtrs[(int)ViewType.SVRSRC] = m_configViewModelS;
            ViewModelPtrs[(int)ViewType.USRSRC] = m_configViewModelU;
            ViewModelPtrs[(int)ViewType.SVRDEST] = m_configViewModelSDest;
            ViewModelPtrs[(int)ViewType.USRDEST] = m_configViewModelUDest;
            ViewModelPtrs[(int)ViewType.OPTIONS] = m_optionsViewModel;
            ViewModelPtrs[(int)ViewType.USERS] = m_usersViewModel;
            ViewModelPtrs[(int)ViewType.SCHED] = m_scheduleViewModel;
            ViewModelPtrs[(int)ViewType.RESULTS] = m_resultsViewModel;
        }


        public void SetupViews(bool isBrowser)
        {
            BaseViewModel.isServer = true;  // because we start out with Server on -- wouldn't get set by command

            m_configViewModelS = new ConfigViewModelS();
            m_configViewModelS.Name = "ConfigViewModelS";
            m_configViewModelS.ViewTitle = "Source";
            m_configViewModelS.lb = lb;
            m_configViewModelS.isBrowser = isBrowser;
            m_configViewModelS.OutlookProfile = "";
            m_configViewModelS.MailServerHostName = "";
            m_configViewModelS.MailServerAdminID = "";
            m_configViewModelS.MailServerAdminPwd = "";
            
            m_configViewModelU = new ConfigViewModelU();
            m_configViewModelU.Name = "ConfigViewModelU";
            m_configViewModelU.ViewTitle = "Source";
            m_configViewModelU.lb = lb;
            m_configViewModelU.isBrowser = isBrowser;
            m_configViewModelU.OutlookProfile = "";
            m_configViewModelU.PSTFile = "";
            m_configViewModelU.OutlookProfile = "";

            m_configViewModelSDest = new ConfigViewModelSDest();
            m_configViewModelSDest.Name = "ConfigViewModelSDest";
            m_configViewModelSDest.ViewTitle = "Destination";
            m_configViewModelSDest.lb = lb;
            m_configViewModelSDest.isBrowser = isBrowser;
            m_configViewModelSDest.ZimbraServerHostName = "";
            m_configViewModelSDest.ZimbraPort = "";
            m_configViewModelSDest.ZimbraAdmin = "";
            m_configViewModelSDest.ZimbraAdminPasswd = "";
            m_configViewModelSDest.ZimbraSSL = true;

            m_configViewModelUDest = new ConfigViewModelUDest();
            m_configViewModelUDest.Name = "ConfigViewModelUDest";
            m_configViewModelUDest.ViewTitle = "Destination";
            m_configViewModelUDest.lb = lb;
            m_configViewModelUDest.isBrowser = isBrowser;
            m_configViewModelUDest.ZimbraServerHostName = "";
            m_configViewModelUDest.ZimbraPort = "";
            m_configViewModelUDest.ZimbraUser = "";
            m_configViewModelUDest.ZimbraUserPasswd = "";
            m_configViewModelUDest.ZimbraSSL = true;

            m_optionsViewModel = new OptionsViewModel();
            m_optionsViewModel.Name = "OptionsViewModel";
            m_optionsViewModel.ViewTitle = "Options";
            m_optionsViewModel.lb = lb;     
            m_optionsViewModel.isBrowser = isBrowser;
            m_optionsViewModel.ImportMailOptions = true;
            m_optionsViewModel.ImportTaskOptions = true;
            m_optionsViewModel.ImportCalendarOptions = true;
            m_optionsViewModel.ImportContactOptions = true;
            m_optionsViewModel.ImportRuleOptions = true;
            m_optionsViewModel.ImportJunkOptions = false;
            m_optionsViewModel.ImportDeletedItemOptions = false;
            m_optionsViewModel.ImportSentOptions = false;
            m_optionsViewModel.MigrateONRAfter = DateTime.Now.ToShortDateString();

            m_scheduleViewModel = new ScheduleViewModel();
            m_scheduleViewModel.Name = "Schedule";
            m_scheduleViewModel.ViewTitle = "Migrate";
            m_scheduleViewModel.lb = lb;
            m_scheduleViewModel.isBrowser = isBrowser;
            m_scheduleViewModel.COS = "default";
            m_scheduleViewModel.DefaultPWD = "";
            m_scheduleViewModel.ScheduleDate = DateTime.Now.ToShortDateString();

            m_usersViewModel = new UsersViewModel("", "");
            m_usersViewModel.Name = "Users";
            m_usersViewModel.ViewTitle = "Users";
            m_usersViewModel.lb = lb;
            m_usersViewModel.ZimbraDomain = "";
            m_usersViewModel.isBrowser = isBrowser;
            m_usersViewModel.CurrentUserSelection = -1;

            m_resultsViewModel = new AccountResultsViewModel(m_scheduleViewModel, -1, 0, "", "", "", 0, "", 0, 0, false);
            m_resultsViewModel.Name = "Results";
            m_resultsViewModel.ViewTitle = "Results";
            m_resultsViewModel.isBrowser = isBrowser;
            m_resultsViewModel.CurrentAccountSelection = -1;

            SetupViewModelPtrs();

            TheViews = new ObservableCollection<object>();
            TheViews.Add(this);
        }
    }
}
