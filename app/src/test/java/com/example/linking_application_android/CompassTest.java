package com.example.linking_application_android.compass;


import com.google.android.gms.maps.model.LatLng;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class CompassTest {

    private CompassUtil compassUtil;


    @BeforeEach
    public void setUp() throws Exception {
        compassUtil = new CompassUtil();

    }

    @Test
    @DisplayName("Check Asset Rotation")
    public void testRotate() {
        assertEquals(198.22393798828125, compassUtil.getAssetRotation(137.0f,
                new LatLng(1.3419264,103.9640436),
                new LatLng(1.3536601066589355,103.93875885009766)));
        assertEquals(297.2347412109375, compassUtil.getAssetRotation(38.0f,
                new LatLng(1.3419194,103.9640461),
                new LatLng(1.3536601066589355,103.93875885009766)));
    }


//    @RepeatedTest(5)
//    @DisplayName("Ensure correct handling of zero")
//    public void testMultiplyWithZero() {
//        assertEquals(198.22393798828125, compassUtil.getAssetRotation(137.0f,
//                new LatLng(1.3419264,103.9640436), new LatLng(1.3536601066589355,103.93875885009766)));
//        assertEquals(198.22393798828125, compassUtil.getAssetRotation(137.0f,
//                new LatLng(1.3419264,103.9640436), new LatLng(1.3536601066589355,103.93875885009766)));
//    }
}