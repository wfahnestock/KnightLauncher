package com.luuqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.editor.EditorsEventHandler;
import com.luuqui.launcher.editor.EditorsGUI;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModListGUI;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsGUI;
import com.luuqui.util.ColorUtil;
import com.luuqui.util.DesktopUtil;
import com.luuqui.util.GifDecoder;
import com.luuqui.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.Log.log;

public class LauncherGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame launcherGUIFrame;

  // Dynamic text
  public static String currentWarning = "";
  public static String latestRelease = "";
  public static String latestChangelog = "";

  public static boolean displayAnimBanner = false;

  public static boolean serverSwitchingEnabled = true;

  public LauncherGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.launcherGUIFrame.setVisible(!this.launcherGUIFrame.isVisible());
  }

  /** @wbp.parser.entryPoint */
  private void initialize() {

    launcherGUIFrame = new JFrame();
    launcherGUIFrame.setVisible(false);
    launcherGUIFrame.setTitle(Locale.getValue("t.main", LauncherGlobals.LAUNCHER_VERSION));
    launcherGUIFrame.setResizable(false);
    launcherGUIFrame.setBounds(100, 100, 1100, 550);
    launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    launcherGUIFrame.setUndecorated(true);
    launcherGUIFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-256.png"));
    launcherGUIFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    launcherGUIFrame.getContentPane().setLayout(null);

    serverSwitcherPane = new JPanel();
    serverSwitcherPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPane.setVisible(true);
    serverSwitcherPane.setBounds(0, 42, 50, 550);
    launcherGUIFrame.getContentPane().add(serverSwitcherPane);

    serverSwitcherPaneScrollBar = new JScrollPane(serverSwitcherPane);
    serverSwitcherPaneScrollBar.setBounds(0, 42, 50, 550);
    serverSwitcherPaneScrollBar.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setBorder(null);
    serverSwitcherPaneScrollBar.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    serverSwitcherPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    serverSwitcherPaneScrollBar.setVisible(true);
    launcherGUIFrame.getContentPane().add(serverSwitcherPaneScrollBar);

    JPanel sidePane = new JPanel();
    sidePane.setBackground(CustomColors.INTERFACE_SIDEPANE_BACKGROUND);
    sidePane.setVisible(true);
    sidePane.setLayout(null);
    sidePane.setBounds(50, 35, 250, 550);
    launcherGUIFrame.getContentPane().add(sidePane);

    banner = ImageUtil.generatePlainColorImage(800, 550, CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    mainPane = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(banner, 0, 0, null);
      }
    };
    mainPane.setLayout(null);
    mainPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    mainPane.setBounds(300, 35, 800, 550);
    launcherGUIFrame.getContentPane().add(mainPane);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/img/icon-92.png");
    BufferedImage launcherLogoImageHover = ImageUtil.loadImageWithinJar("/img/icon-92-hover.png");
    launcherLogo.setBounds(0, -27, 251, 200);
    launcherLogo.setHorizontalAlignment(SwingConstants.CENTER);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    sidePane.add(launcherLogo);
    launcherLogo.addMouseListener(new MouseListener() {
      @Override public void mouseClicked(MouseEvent e) {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      }
      @Override public void mousePressed(MouseEvent e) {}
      @Override public void mouseReleased(MouseEvent e) {}
      @Override public void mouseEntered(MouseEvent e) {
        launcherLogo.setIcon(new ImageIcon(launcherLogoImageHover));
        launcherLogo.updateUI();
      }
      @Override public void mouseExited(MouseEvent e) {
        launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
        launcherLogo.updateUI();
      }
    });

    JLabel launcherName = new JLabel(LauncherGlobals.LAUNCHER_NAME);
    launcherName.setFont(Fonts.fontMedBig);
    launcherName.setHorizontalAlignment(SwingConstants.CENTER);
    launcherName.setVerticalAlignment(SwingConstants.CENTER);
    launcherName.setBounds(0, 100, 250, 80);
    sidePane.add(launcherName);

    selectedServerLabel = new JLabel(Locale.getValue("m.server", "Official"));
    selectedServerLabel.setFont(Fonts.fontMed);
    selectedServerLabel.setBounds(28, 185, 120, 20);
    sidePane.add(selectedServerLabel);

    serverInfoButton = new JButton();
    serverInfoButton.setBounds(80, 185, 130, 20);
    serverInfoButton.setEnabled(false);
    serverInfoButton.setVisible(false);
    serverInfoButton.setFocusable(false);
    serverInfoButton.setFocusPainted(false);
    serverInfoButton.setForeground(Color.WHITE);
    serverInfoButton.setToolTipText(Locale.getValue("m.server_info"));
    serverInfoButton.addActionListener(l -> LauncherEventHandler.displaySelectedServerInfo());
    sidePane.add(serverInfoButton);

    Icon playerCountIcon = IconFontSwing.buildIcon(FontAwesome.USERS, 14, CustomColors.INTERFACE_DEFAULT);
    playerCountLabel = new JLabel(Locale.getValue("m.players_online_load"));
    playerCountLabel.setFont(Fonts.fontReg);
    playerCountLabel.setIcon(playerCountIcon);
    playerCountLabel.setBounds(28, 210, 200, 18);
    sidePane.add(playerCountLabel);

    String playerCountTooltipTitle = Locale.getValue("m.players_online");
    String playerCountTooltipText = Locale.getValue("m.players_online_text");
    Icon playerCountTooltipButtonIcon = IconFontSwing.buildIcon(FontAwesome.QUESTION, 12, Color.WHITE);
    playerCountTooltipButton = new JButton();
    playerCountTooltipButton.setIcon(playerCountTooltipButtonIcon);
    playerCountTooltipButton.setBounds(173, 213, 13, 13);
    playerCountTooltipButton.setEnabled(true);
    playerCountTooltipButton.setFocusable(false);
    playerCountTooltipButton.setFocusPainted(false);
    playerCountTooltipButton.setBorderPainted(false);
    playerCountTooltipButton.setForeground(Color.WHITE);
    playerCountTooltipButton.setToolTipText(playerCountTooltipTitle);
    playerCountTooltipButton.addActionListener(l -> {
      Dialog.push(playerCountTooltipText, playerCountTooltipTitle, JOptionPane.INFORMATION_MESSAGE);
    });
    sidePane.add(playerCountTooltipButton);
    playerCountTooltipButton.setVisible(false);

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
    settingsButton = new JButton(Locale.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(28, 275, 125, 35);
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(Fonts.fontMed);
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setBorderPainted(false);
    settingsButton.setBackground(CustomColors.INTERFACE_SIDEPANE_BUTTON);
    settingsButton.setForeground(Color.WHITE);
    settingsButton.setToolTipText(Locale.getValue("b.settings"));
    settingsButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredModsPane.setVisible(false);
      layeredEditorsPane.setVisible(false);

      layeredSettingsPane = SettingsGUI.tabbedPane;
      layeredSettingsPane.setBounds(300, 75, 800, 550);
      launcherGUIFrame.add(layeredSettingsPane);
      layeredSettingsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      launcherGUIFrame.add(layeredReturnButton);
    });
    sidePane.add(settingsButton);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(Locale.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(28, 315, 125, 35);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.fontMed);
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setBorderPainted(false);
    modButton.setEnabled(true);
    modButton.setBackground(CustomColors.INTERFACE_SIDEPANE_BUTTON);
    modButton.setForeground(Color.WHITE);
    modButton.setToolTipText(Locale.getValue("b.mods"));
    modButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredSettingsPane.setVisible(false);
      layeredEditorsPane.setVisible(false);

      layeredModsPane = ModListGUI.modListPanel;
      layeredModsPane.setBounds(300, 75, 800, 550);
      launcherGUIFrame.add(layeredModsPane);
      layeredModsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      launcherGUIFrame.add(layeredReturnButton);
    });
    sidePane.add(modButton);

    Icon editorsIcon = IconFontSwing.buildIcon(FontAwesome.PENCIL, 16, ColorUtil.getForegroundColor());
    editorsButton = new JButton(Locale.getValue("b.editors"));
    editorsButton.setIcon(editorsIcon);
    editorsButton.setBounds(28, 355, 125, 35);
    editorsButton.setHorizontalAlignment(SwingConstants.LEFT);
    editorsButton.setFont(Fonts.fontMed);
    editorsButton.setFocusPainted(false);
    editorsButton.setFocusable(false);
    editorsButton.setBorderPainted(false);
    editorsButton.setEnabled(true);
    editorsButton.setBackground(CustomColors.INTERFACE_SIDEPANE_BUTTON);
    editorsButton.setForeground(Color.WHITE);
    editorsButton.setToolTipText(Locale.getValue("b.editors"));
    editorsButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredSettingsPane.setVisible(false);
      layeredModsPane.setVisible(false);

      layeredEditorsPane = EditorsGUI.editorsPanel;
      layeredEditorsPane.setBounds(300, 75, 800, 550);
      launcherGUIFrame.add(layeredEditorsPane);
      layeredEditorsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      launcherGUIFrame.add(layeredReturnButton);
    });
    sidePane.add(editorsButton);

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/img/icon-discord.png")));
    discordButton.setBounds(65, 440, 36, 36);
    discordButton.setToolTipText(Locale.getValue("b.discord"));
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBorderPainted(false);
    discordButton.setBackground(CustomColors.INTERFACE_SIDEPANE_BUTTON);
    discordButton.setFont(Fonts.fontMed);
    sidePane.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DISCORD));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 17, Color.WHITE);
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(107, 440, 36, 36);
    bugButton.setToolTipText(Locale.getValue("b.bug_report"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorderPainted(false);
    bugButton.setBackground(CustomColors.INTERFACE_SIDEPANE_BUTTON);
    bugButton.setFont(Fonts.fontMed);
    sidePane.add(bugButton);
    bugButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_BUG_REPORT));

    Icon donateIcon = IconFontSwing.buildIcon(FontAwesome.USD, 17, Color.WHITE);
    JButton donateButton = new JButton(donateIcon);
    donateButton.setBounds(149, 440, 36, 36);
    donateButton.setToolTipText(Locale.getValue("b.donate"));
    donateButton.setFocusPainted(false);
    donateButton.setFocusable(false);
    donateButton.setBorderPainted(false);
    donateButton.setBackground(CustomColors.PREMIUM);
    donateButton.setFont(Fonts.fontMed);
    sidePane.add(donateButton);
    donateButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DONATE));

    JLabel launcherVersion = new JLabel("v" + LauncherGlobals.LAUNCHER_VERSION);
    launcherVersion.setFont(Fonts.fontRegSmall);
    launcherVersion.setForeground(CustomColors.INTERFACE_SIDEPANE_FOOTNOTE);
    launcherVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    launcherVersion.setBounds(10, 493, 230, 15);
    sidePane.add(launcherVersion);

    bannerTitle = new JLabel(Locale.getValue("m.banner_title_default"));
    bannerTitle.setBounds(35, -60, 700, 340);
    bannerTitle.setFont(Fonts.fontMedGiant);
    bannerTitle.setForeground(Color.WHITE);
    mainPane.add(bannerTitle);

    bannerSubtitle1 = new JLabel(Locale.getValue("m.banner_subtitle_default"));
    bannerSubtitle1.setBounds(40, -15, 700, 340);
    bannerSubtitle1.setFont(Fonts.fontMedBig);
    bannerSubtitle1.setForeground(Color.WHITE);
    mainPane.add(bannerSubtitle1);

    bannerSubtitle2 = new JLabel("");
    bannerSubtitle2.setBounds(40, 5, 700, 340);
    bannerSubtitle2.setFont(Fonts.fontMedBig);
    bannerSubtitle2.setForeground(Color.WHITE);
    mainPane.add(bannerSubtitle2);

    bannerLinkButton = new JButton(Locale.getValue("b.learn_more"));
    bannerLinkButton.setBounds(40, 195, 110, 25);
    bannerLinkButton.setFont(Fonts.fontMed);
    bannerLinkButton.setForeground(Color.WHITE);
    bannerLinkButton.setFocusPainted(false);
    bannerLinkButton.setFocusable(false);
    bannerLinkButton.setOpaque(false);
    bannerLinkButton.setBackground(CustomColors.INTERFACE_MAINPANE_BUTTON);
    bannerLinkButton.setBorderPainted(false);
    bannerLinkButton.setVisible(false);
    mainPane.add(bannerLinkButton);

    launchButton = new JButton(Locale.getValue("b.play_now"));
    launchButton.setBounds(572, 423, 200, 66);
    launchButton.setFont(Fonts.fontMedBig);
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.setBorderPainted(false);
    launchButton.setForeground(Color.WHITE);
    launchButton.setToolTipText(Locale.getValue("b.play_now"));
    mainPane.add(launchButton);
    launchButton.addActionListener(action -> {
      if (KeyboardController.isShiftPressed() || KeyboardController.isAltPressed()) {
        // TODO: Consolidate alt launching inside LauncherEventHandler::launchGameEvent for both.
        if(LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
          LauncherEventHandler.launchGameAltEvent();
        } else {
          LauncherEventHandler.launchGameEvent(true);
        }
      } else {
        LauncherEventHandler.launchGameEvent(false);
      }
    });

    String launchTooltipTitle = Locale.getValue("m.alt_mode");
    String launchTooltipText = Locale.getValue("m.alt_mode_text");
    Icon launchTooltipButtonIcon = IconFontSwing.buildIcon(FontAwesome.QUESTION, 16, Color.WHITE);
    JButton launchTooltipButton = new JButton();
    launchTooltipButton.setIcon(launchTooltipButtonIcon);
    launchTooltipButton.setBounds(548, 424, 20, 20);
    launchTooltipButton.setEnabled(true);
    launchTooltipButton.setFocusable(false);
    launchTooltipButton.setFocusPainted(false);
    launchTooltipButton.setBorderPainted(false);
    launchTooltipButton.setBackground(CustomColors.INTERFACE_MAINPANE_BUTTON);
    launchTooltipButton.setForeground(Color.WHITE);
    launchTooltipButton.setToolTipText(launchTooltipTitle);
    launchTooltipButton.addActionListener(l -> {
      Dialog.push(launchTooltipText, launchTooltipTitle, JOptionPane.INFORMATION_MESSAGE);
    });
    mainPane.add(launchTooltipButton);

    BufferedImage launchBackgroundImage = ImageUtil.generatePlainColorImage(500, 85, new Color(0, 0, 0));
    launchBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(launchBackgroundImage, 25);
    ImageUtil.setAlpha(launchBackgroundImage, (byte) 191);
    launchBackground = new JLabel("");
    launchBackground.setBounds(20, 410, 500, 85);
    launchBackground.setIcon(new ImageIcon(launchBackgroundImage));
    launchBackground.setVisible(false);
    mainPane.add(launchBackground);
    mainPane.setComponentZOrder(launchBackground, 1);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.LEFT);
    launchState.setBounds(35, 420, 505, 25);
    launchState.setFont(Fonts.fontRegBig);
    launchState.setVisible(false);
    mainPane.add(launchState);
    mainPane.setComponentZOrder(launchState, 0);

    launchProgressBar = new JProgressBar();
    launchProgressBar.setBounds(35, 450, 470, 25);
    launchProgressBar.setVisible(false);
    mainPane.add(launchProgressBar);
    mainPane.setComponentZOrder(launchProgressBar, 0);

    Icon changelogIcon = IconFontSwing.buildIcon(FontAwesome.BOOK, 18, Color.WHITE);
    changelogButton = new JButton(changelogIcon);
    changelogButton.setBounds(736, 26, 36, 36);
    changelogButton.setToolTipText(Locale.getValue("m.changelog"));
    changelogButton.setFont(Fonts.fontMed);
    changelogButton.setFocusPainted(false);
    changelogButton.setFocusable(false);
    changelogButton.setBorderPainted(false);
    changelogButton.setBackground(CustomColors.CHANGELOGS);
    changelogButton.setForeground(Color.WHITE);
    changelogButton.setVisible(true);
    mainPane.add(changelogButton);
    changelogButton.addActionListener(l -> LauncherEventHandler.showLatestChangelog());

    Icon warningNoticeIcon = IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 16, Color.WHITE);
    warningNotice = new JButton(warningNoticeIcon);
    warningNotice.setBounds(691, 26, 36, 36);
    warningNotice.setToolTipText(Locale.getValue("m.warning_notice"));
    warningNotice.setFocusPainted(false);
    warningNotice.setFocusable(false);
    warningNotice.setBorderPainted(false);
    warningNotice.setForeground(Color.WHITE);
    warningNotice.setBackground(CustomColors.MID_RED);
    warningNotice.setFont(Fonts.fontMed);
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(currentWarning, Locale.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    mainPane.add(warningNotice);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 16, Color.WHITE);
    updateButton = new JButton(updateIcon);
    updateButton.setBounds(691, 26, 36, 36);
    updateButton.setToolTipText(Locale.getValue("b.update"));
    updateButton.setFont(Fonts.fontMed);
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setBorderPainted(false);
    updateButton.setBackground(CustomColors.UPDATE);
    updateButton.setForeground(Color.WHITE);
    updateButton.setVisible(false);
    mainPane.add(updateButton);
    updateButton.addActionListener(l -> LauncherEventHandler.updateLauncher());

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    playAnimatedBannersButton = new JButton(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    playAnimatedBannersButton.setBounds(736, 71, 36, 36);
    playAnimatedBannersButton.setToolTipText(Locale.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    playAnimatedBannersButton.setFont(Fonts.fontMed);
    playAnimatedBannersButton.setFocusPainted(false);
    playAnimatedBannersButton.setFocusable(false);
    playAnimatedBannersButton.setBorderPainted(false);
    playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_SIDEPANE_BUTTON : CustomColors.MID_RED);
    playAnimatedBannersButton.setForeground(Color.WHITE);
    playAnimatedBannersButton.setVisible(false);
    mainPane.add(playAnimatedBannersButton);
    playAnimatedBannersButton.addActionListener(l -> LauncherEventHandler.switchBannerAnimations());

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, launcherGUIFrame.getWidth(), 35);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    launcherGUIFrame.getContentPane().add(titleBar);


    /*
     * Based on Paul Samsotha's reply @ StackOverflow
     * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
     */
    titleBar.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }
    });
    titleBar.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }

      @Override
      public void mouseDragged(MouseEvent me) {

        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
                launcherGUIFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
                launcherGUIFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    /*
    JLabel windowTitle = new JLabel(Locale.getValue("t.main", LauncherGlobals.LAUNCHER_VERSION));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 35);
    titleBar.add(windowTitle);
     */

    final int BUTTON_WIDTH = 35;
    final int BUTTON_HEIGHT = 35;

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 17, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(launcherGUIFrame.getWidth() - BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBackground(null);
    closeButton.setBorder(null);
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> {
      DiscordRPC.getInstance().stop();
      System.exit(0);
    });
    closeButton.addMouseListener(new MouseListener() {
      @Override public void mouseClicked(MouseEvent e) {}
      @Override public void mousePressed(MouseEvent e) {}
      @Override public void mouseReleased(MouseEvent e) {}
      @Override public void mouseEntered(MouseEvent e) {
        closeButton.setBackground(CustomColors.MID_RED);
      }
      @Override public void mouseExited(MouseEvent e) {
        closeButton.setBackground(null);
      }
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 12, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(launcherGUIFrame.getWidth() - BUTTON_WIDTH * 2, -7, BUTTON_WIDTH, BUTTON_HEIGHT + 7);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBackground(null);
    minimizeButton.setBorder(null);
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(e -> launcherGUIFrame.setState(Frame.ICONIFIED));

    launcherGUIFrame.setLocationRelativeTo(null);

    launcherGUIFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        DiscordRPC.getInstance().stop();
      }
    });

  }

  public static void showWarning(String message) {
    // we're also showing an available update, lets move the warning notice
    // slightly to the left, so they don't overlap.
    if(updateButton.isVisible()) {
      warningNotice.setBounds(warningNotice.getX() - 45, 26, 35, 35);
    }

    warningNotice.setVisible(true);
    currentWarning = message;
  }

  public static BufferedImage processImageForBanner(BufferedImage image, double intensity) {
    image = ImageUtil.resizeImage(image, 800, 550);
    image = ImageUtil.fadeEdges(image, intensity);
    return image;
  }

  public static void processAnimatedImageForBanner(byte[] gifData, double intensity) {
    try {
      final GifDecoder.GifImage gif = GifDecoder.read(gifData);
      final int frameCount = gif.getFrameCount();
      final java.util.List<BufferedImage> proccesedImages = new ArrayList<>();

      // process every single frame of the gif.
      for (int i = 0; i < frameCount; i++) {
        BufferedImage frame = gif.getFrame(i);
        frame = ImageUtil.resizeImage(frame, 800, 550);
        if(intensity > 0) ImageUtil.fadeEdges(frame, intensity);
        proccesedImages.add(frame);
      }

      displayAnimBanner = true;
      new Thread(() -> {
        while(displayAnimBanner) {
          for (int i = 0; i < proccesedImages.size(); i++) {
            // we might need to end prematurely to avoid concurrent modifications.
            if(!displayAnimBanner) break;

            try {
              Thread.sleep(gif.getDelay(i) * 10);
            } catch (InterruptedException e) {
              log.error(e);
            }

            if(Settings.playAnimatedBanners) {
              // set the new frame.
              banner = proccesedImages.get(i);
              mainPane.repaint();
            } else {
              banner = proccesedImages.get(0);
              mainPane.repaint();
            }
          }
        }
      }).start();

    } catch (IOException e) {
      log.error(e);
    }
  }

  protected static void specialKeyPressed() {
    launchButton.setBackground(CustomColors.LAUNCH_ALT);
    launchButton.updateUI();
  }

  protected static void specialKeyReleased() {
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.updateUI();
  }

  // Shared
  public static JTabbedPane layeredSettingsPane = new JTabbedPane();
  public static JPanel layeredModsPane = new JPanel();
  public static JPanel layeredEditorsPane = new JPanel();
  public static JButton layeredReturnButton;

  // Server switcher pane
  public static JPanel serverSwitcherPane;
  public static JScrollPane serverSwitcherPaneScrollBar;

  // Side pane
  public static JButton settingsButton;
  public static JButton modButton;
  public static JButton editorsButton;
  public static JButton playerCountTooltipButton;
  public static JLabel playerCountLabel;
  public static JLabel selectedServerLabel;
  public static JButton serverInfoButton;

  // Main pane
  public static JPanel mainPane;
  public static BufferedImage banner = null;
  public static JLabel bannerTitle;
  public static JLabel bannerSubtitle1;
  public static JLabel bannerSubtitle2;
  public static JButton bannerLinkButton;
  public static JButton launchButton;
  public static JButton updateButton;
  public static JButton changelogButton;
  public static JButton playAnimatedBannersButton;
  public static JLabel launchBackground;
  public static JLabel launchState;
  public static JProgressBar launchProgressBar = new JProgressBar();
  public static JButton warningNotice;

}
