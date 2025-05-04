package com.eactive.levelup.controller.view;

import com.eactive.levelup.controller.constant.BreadcrumbTitle;
import com.eactive.levelup.controller.constant.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    @GetMapping
    public String pageMain(Model model) {
        model.addAttribute("pageId", Menu.DASHBOARD);
        model.addAttribute("breadcrumbTitle", BreadcrumbTitle.DASHBOARD);
        return "pages/dashboard/dashboard-main";
    }

}
