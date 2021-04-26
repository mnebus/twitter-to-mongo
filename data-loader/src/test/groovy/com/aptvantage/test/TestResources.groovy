package com.aptvantage.test

class TestResources {

    static String getResource(String resourceName) {
        return TestResources.getClassLoader().getResource(resourceName).getText()
    }
}
