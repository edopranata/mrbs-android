/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ropekanbaru.booking.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.vector.ImageVector

// Ikon Material (gaya Outlined) yang dipakai agar tampilan serasi dengan ikon lucide di versi web.
// Disalin dari androidx.compose.material:material-icons-extended 1.7.8 (Apache 2.0) supaya APK tidak
// membawa seluruh pustaka ikon (ribuan ikon), hanya yang dipakai.

public val Icons.Outlined.DoorFront: ImageVector
    get() {
        if (_doorFront != null) {
            return _doorFront!!
        }
        _doorFront = materialIcon(name = "Outlined.DoorFront") {
            materialPath {
                moveTo(19.0f, 19.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 3.0f, 5.0f, 3.9f, 5.0f, 5.0f)
                verticalLineToRelative(14.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(19.0f)
                close()
                moveTo(17.0f, 19.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(13.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(11.0f)
                close()
            }
        }
        return _doorFront!!
    }

private var _doorFront: ImageVector? = null

public val Icons.Outlined.EventAvailable: ImageVector
    get() {
        if (_eventAvailable != null) {
            return _eventAvailable!!
        }
        _eventAvailable = materialIcon(name = "Outlined.EventAvailable") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(18.0f, 1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 3.0f)
                lineTo(8.0f, 1.0f)
                lineTo(6.0f, 1.0f)
                verticalLineToRelative(2.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                lineTo(5.0f, 9.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(10.0f)
                close()
                moveTo(5.0f, 7.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                lineTo(5.0f, 7.0f)
                close()
                moveTo(10.56f, 17.46f)
                lineToRelative(5.93f, -5.93f)
                lineToRelative(-1.06f, -1.06f)
                lineToRelative(-4.87f, 4.87f)
                lineToRelative(-2.11f, -2.11f)
                lineToRelative(-1.06f, 1.06f)
                close()
            }
        }
        return _eventAvailable!!
    }

private var _eventAvailable: ImageVector? = null

public val Icons.Outlined.EditCalendar: ImageVector
    get() {
        if (_editCalendar != null) {
            return _editCalendar!!
        }
        _editCalendar = materialIcon(name = "Outlined.EditCalendar") {
            materialPath {
                moveTo(5.0f, 10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(5.0f, 6.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(22.84f, 16.28f)
                lineToRelative(-0.71f, 0.71f)
                lineToRelative(-2.12f, -2.12f)
                lineToRelative(0.71f, -0.71f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(0.71f, 0.71f)
                curveTo(23.23f, 15.26f, 23.23f, 15.89f, 22.84f, 16.28f)
                close()
                moveTo(19.3f, 15.58f)
                lineToRelative(2.12f, 2.12f)
                lineToRelative(-5.3f, 5.3f)
                horizontalLineTo(14.0f)
                verticalLineToRelative(-2.12f)
                lineTo(19.3f, 15.58f)
                close()
            }
        }
        return _editCalendar!!
    }

private var _editCalendar: ImageVector? = null

public val Icons.Outlined.Group: ImageVector
    get() {
        if (_group != null) {
            return _group!!
        }
        _group = materialIcon(name = "Outlined.Group") {
            materialPath {
                moveTo(9.0f, 13.75f)
                curveToRelative(-2.34f, 0.0f, -7.0f, 1.17f, -7.0f, 3.5f)
                lineTo(2.0f, 19.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-1.75f)
                curveToRelative(0.0f, -2.33f, -4.66f, -3.5f, -7.0f, -3.5f)
                close()
                moveTo(4.34f, 17.0f)
                curveToRelative(0.84f, -0.58f, 2.87f, -1.25f, 4.66f, -1.25f)
                reflectiveCurveToRelative(3.82f, 0.67f, 4.66f, 1.25f)
                lineTo(4.34f, 17.0f)
                close()
                moveTo(9.0f, 12.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(10.93f, 5.0f, 9.0f, 5.0f)
                reflectiveCurveTo(5.5f, 6.57f, 5.5f, 8.5f)
                reflectiveCurveTo(7.07f, 12.0f, 9.0f, 12.0f)
                close()
                moveTo(9.0f, 7.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(9.83f, 10.0f, 9.0f, 10.0f)
                reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(8.17f, 7.0f, 9.0f, 7.0f)
                close()
                moveTo(16.04f, 13.81f)
                curveToRelative(1.16f, 0.84f, 1.96f, 1.96f, 1.96f, 3.44f)
                lineTo(18.0f, 19.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-1.75f)
                curveToRelative(0.0f, -2.02f, -3.5f, -3.17f, -5.96f, -3.44f)
                close()
                moveTo(15.0f, 12.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(16.93f, 5.0f, 15.0f, 5.0f)
                curveToRelative(-0.54f, 0.0f, -1.04f, 0.13f, -1.5f, 0.35f)
                curveToRelative(0.63f, 0.89f, 1.0f, 1.98f, 1.0f, 3.15f)
                reflectiveCurveToRelative(-0.37f, 2.26f, -1.0f, 3.15f)
                curveToRelative(0.46f, 0.22f, 0.96f, 0.35f, 1.5f, 0.35f)
                close()
            }
        }
        return _group!!
    }

private var _group: ImageVector? = null

public val Icons.Outlined.Apartment: ImageVector
    get() {
        if (_apartment != null) {
            return _apartment!!
        }
        _apartment = materialIcon(name = "Outlined.Apartment") {
            materialPath {
                moveTo(17.0f, 11.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(8.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(17.0f)
                close()
                moveTo(7.0f, 19.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(7.0f, 15.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(7.0f, 11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 15.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 7.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(15.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(19.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _apartment!!
    }

private var _apartment: ImageVector? = null

public val Icons.Outlined.CalendarMonth: ImageVector
    get() {
        if (_calendarMonth != null) {
            return _calendarMonth!!
        }
        _calendarMonth = materialIcon(name = "Outlined.CalendarMonth") {
            materialPath {
                moveTo(19.0f, 4.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(21.0f, 4.9f, 20.1f, 4.0f, 19.0f, 4.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(19.0f, 8.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(8.0f)
                close()
                moveTo(9.0f, 14.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(17.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(9.0f, 18.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(17.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _calendarMonth!!
    }

private var _calendarMonth: ImageVector? = null

public val Icons.Outlined.DeleteOutline: ImageVector
    get() {
        if (_deleteOutline != null) {
            return _deleteOutline!!
        }
        _deleteOutline = materialIcon(name = "Outlined.DeleteOutline") {
            materialPath {
                moveTo(6.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(18.0f, 7.0f)
                lineTo(6.0f, 7.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(8.0f, 9.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(10.0f)
                lineTo(8.0f, 19.0f)
                lineTo(8.0f, 9.0f)
                close()
                moveTo(15.5f, 4.0f)
                lineToRelative(-1.0f, -1.0f)
                horizontalLineToRelative(-5.0f)
                lineToRelative(-1.0f, 1.0f)
                lineTo(5.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                lineTo(19.0f, 4.0f)
                horizontalLineToRelative(-3.5f)
                close()
            }
        }
        return _deleteOutline!!
    }

private var _deleteOutline: ImageVector? = null

public val Icons.Outlined.Cancel: ImageVector
    get() {
        if (_cancel != null) {
            return _cancel!!
        }
        _cancel = materialIcon(name = "Outlined.Cancel") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.47f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
                reflectiveCurveTo(17.53f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(15.59f, 7.0f)
                lineTo(12.0f, 10.59f)
                lineTo(8.41f, 7.0f)
                lineTo(7.0f, 8.41f)
                lineTo(10.59f, 12.0f)
                lineTo(7.0f, 15.59f)
                lineTo(8.41f, 17.0f)
                lineTo(12.0f, 13.41f)
                lineTo(15.59f, 17.0f)
                lineTo(17.0f, 15.59f)
                lineTo(13.41f, 12.0f)
                lineTo(17.0f, 8.41f)
                close()
            }
        }
        return _cancel!!
    }

private var _cancel: ImageVector? = null

public val Icons.Outlined.Schedule: ImageVector
    get() {
        if (_schedule != null) {
            return _schedule!!
        }
        _schedule = materialIcon(name = "Outlined.Schedule") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(12.5f, 7.0f)
                lineTo(11.0f, 7.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
        return _schedule!!
    }

private var _schedule: ImageVector? = null

public val Icons.Outlined.PersonOutline: ImageVector
    get() {
        if (_personOutline != null) {
            return _personOutline!!
        }
        _personOutline = materialIcon(name = "Outlined.PersonOutline") {
            materialPath {
                moveTo(12.0f, 5.9f)
                curveToRelative(1.16f, 0.0f, 2.1f, 0.94f, 2.1f, 2.1f)
                reflectiveCurveToRelative(-0.94f, 2.1f, -2.1f, 2.1f)
                reflectiveCurveTo(9.9f, 9.16f, 9.9f, 8.0f)
                reflectiveCurveToRelative(0.94f, -2.1f, 2.1f, -2.1f)
                moveToRelative(0.0f, 9.0f)
                curveToRelative(2.97f, 0.0f, 6.1f, 1.46f, 6.1f, 2.1f)
                verticalLineToRelative(1.1f)
                lineTo(5.9f, 18.1f)
                lineTo(5.9f, 17.0f)
                curveToRelative(0.0f, -0.64f, 3.13f, -2.1f, 6.1f, -2.1f)
                moveTo(12.0f, 4.0f)
                curveTo(9.79f, 4.0f, 8.0f, 5.79f, 8.0f, 8.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
                moveTo(12.0f, 13.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                close()
            }
        }
        return _personOutline!!
    }

private var _personOutline: ImageVector? = null

public val Icons.Outlined.Groups: ImageVector
    get() {
        if (_groups != null) {
            return _groups!!
        }
        _groups = materialIcon(name = "Outlined.Groups") {
            materialPath {
                moveTo(4.0f, 13.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                curveTo(2.0f, 12.1f, 2.9f, 13.0f, 4.0f, 13.0f)
                close()
                moveTo(5.13f, 14.1f)
                curveTo(4.76f, 14.04f, 4.39f, 14.0f, 4.0f, 14.0f)
                curveToRelative(-0.99f, 0.0f, -1.93f, 0.21f, -2.78f, 0.58f)
                curveTo(0.48f, 14.9f, 0.0f, 15.62f, 0.0f, 16.43f)
                verticalLineTo(18.0f)
                lineToRelative(4.5f, 0.0f)
                verticalLineToRelative(-1.61f)
                curveTo(4.5f, 15.56f, 4.73f, 14.78f, 5.13f, 14.1f)
                close()
                moveTo(20.0f, 13.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                curveTo(18.0f, 12.1f, 18.9f, 13.0f, 20.0f, 13.0f)
                close()
                moveTo(24.0f, 16.43f)
                curveToRelative(0.0f, -0.81f, -0.48f, -1.53f, -1.22f, -1.85f)
                curveTo(21.93f, 14.21f, 20.99f, 14.0f, 20.0f, 14.0f)
                curveToRelative(-0.39f, 0.0f, -0.76f, 0.04f, -1.13f, 0.1f)
                curveToRelative(0.4f, 0.68f, 0.63f, 1.46f, 0.63f, 2.29f)
                verticalLineTo(18.0f)
                lineToRelative(4.5f, 0.0f)
                verticalLineTo(16.43f)
                close()
                moveTo(16.24f, 13.65f)
                curveToRelative(-1.17f, -0.52f, -2.61f, -0.9f, -4.24f, -0.9f)
                curveToRelative(-1.63f, 0.0f, -3.07f, 0.39f, -4.24f, 0.9f)
                curveTo(6.68f, 14.13f, 6.0f, 15.21f, 6.0f, 16.39f)
                verticalLineTo(18.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-1.61f)
                curveTo(18.0f, 15.21f, 17.32f, 14.13f, 16.24f, 13.65f)
                close()
                moveTo(8.07f, 16.0f)
                curveToRelative(0.09f, -0.23f, 0.13f, -0.39f, 0.91f, -0.69f)
                curveToRelative(0.97f, -0.38f, 1.99f, -0.56f, 3.02f, -0.56f)
                reflectiveCurveToRelative(2.05f, 0.18f, 3.02f, 0.56f)
                curveToRelative(0.77f, 0.3f, 0.81f, 0.46f, 0.91f, 0.69f)
                horizontalLineTo(8.07f)
                close()
                moveTo(12.0f, 8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveTo(11.45f, 8.0f, 12.0f, 8.0f)
                moveTo(12.0f, 6.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                curveTo(15.0f, 7.34f, 13.66f, 6.0f, 12.0f, 6.0f)
                lineTo(12.0f, 6.0f)
                close()
            }
        }
        return _groups!!
    }

private var _groups: ImageVector? = null

public val Icons.Outlined.Repeat: ImageVector
    get() {
        if (_repeat != null) {
            return _repeat!!
        }
        _repeat = materialIcon(name = "Outlined.Repeat") {
            materialPath {
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineToRelative(3.0f)
                lineTo(5.0f, 5.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                lineTo(7.0f, 7.0f)
                close()
                moveTo(17.0f, 17.0f)
                lineTo(7.0f, 17.0f)
                verticalLineToRelative(-3.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(4.0f, 4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _repeat!!
    }

private var _repeat: ImageVector? = null

public val Icons.Outlined.Inbox: ImageVector
    get() {
        if (_inbox != null) {
            return _inbox!!
        }
        _inbox = materialIcon(name = "Outlined.Inbox") {
            materialPath {
                moveTo(19.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(3.56f)
                curveToRelative(0.69f, 1.19f, 1.97f, 2.0f, 3.45f, 2.0f)
                reflectiveCurveToRelative(2.75f, -0.81f, 3.45f, -2.0f)
                lineTo(19.0f, 16.0f)
                verticalLineToRelative(3.0f)
                close()
                moveTo(19.0f, 14.0f)
                horizontalLineToRelative(-4.99f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                lineTo(5.0f, 14.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(9.0f)
                close()
            }
        }
        return _inbox!!
    }

private var _inbox: ImageVector? = null

public val Icons.Outlined.PlayCircle: ImageVector
    get() {
        if (_playCircle != null) {
            return _playCircle!!
        }
        _playCircle = materialIcon(name = "Outlined.PlayCircle") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveTo(16.41f, 20.0f, 12.0f, 20.0f)
                close()
                moveTo(9.5f, 16.5f)
                lineToRelative(7.0f, -4.5f)
                lineToRelative(-7.0f, -4.5f)
                verticalLineTo(16.5f)
                close()
            }
        }
        return _playCircle!!
    }

private var _playCircle: ImageVector? = null

public val Icons.Outlined.Visibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = materialIcon(name = "Outlined.Visibility") {
            materialPath {
                moveTo(12.0f, 6.0f)
                curveToRelative(3.79f, 0.0f, 7.17f, 2.13f, 8.82f, 5.5f)
                curveTo(19.17f, 14.87f, 15.79f, 17.0f, 12.0f, 17.0f)
                reflectiveCurveToRelative(-7.17f, -2.13f, -8.82f, -5.5f)
                curveTo(4.83f, 8.13f, 8.21f, 6.0f, 12.0f, 6.0f)
                moveToRelative(0.0f, -2.0f)
                curveTo(7.0f, 4.0f, 2.73f, 7.11f, 1.0f, 11.5f)
                curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(1.38f, 0.0f, 2.5f, 1.12f, 2.5f, 2.5f)
                reflectiveCurveTo(13.38f, 14.0f, 12.0f, 14.0f)
                reflectiveCurveToRelative(-2.5f, -1.12f, -2.5f, -2.5f)
                reflectiveCurveTo(10.62f, 9.0f, 12.0f, 9.0f)
                moveToRelative(0.0f, -2.0f)
                curveToRelative(-2.48f, 0.0f, -4.5f, 2.02f, -4.5f, 4.5f)
                reflectiveCurveTo(9.52f, 16.0f, 12.0f, 16.0f)
                reflectiveCurveToRelative(4.5f, -2.02f, 4.5f, -4.5f)
                reflectiveCurveTo(14.48f, 7.0f, 12.0f, 7.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _visibility: ImageVector? = null

public val Icons.Outlined.VisibilityOff: ImageVector
    get() {
        if (_visibilityOff != null) {
            return _visibilityOff!!
        }
        _visibilityOff = materialIcon(name = "Outlined.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 6.0f)
                curveToRelative(3.79f, 0.0f, 7.17f, 2.13f, 8.82f, 5.5f)
                curveToRelative(-0.59f, 1.22f, -1.42f, 2.27f, -2.41f, 3.12f)
                lineToRelative(1.41f, 1.41f)
                curveToRelative(1.39f, -1.23f, 2.49f, -2.77f, 3.18f, -4.53f)
                curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
                curveToRelative(-1.27f, 0.0f, -2.49f, 0.2f, -3.64f, 0.57f)
                lineToRelative(1.65f, 1.65f)
                curveTo(10.66f, 6.09f, 11.32f, 6.0f, 12.0f, 6.0f)
                close()
                moveTo(10.93f, 7.14f)
                lineTo(13.0f, 9.21f)
                curveToRelative(0.57f, 0.25f, 1.03f, 0.71f, 1.28f, 1.28f)
                lineToRelative(2.07f, 2.07f)
                curveToRelative(0.08f, -0.34f, 0.14f, -0.7f, 0.14f, -1.07f)
                curveTo(16.5f, 9.01f, 14.48f, 7.0f, 12.0f, 7.0f)
                curveToRelative(-0.37f, 0.0f, -0.72f, 0.05f, -1.07f, 0.14f)
                close()
                moveTo(2.01f, 3.87f)
                lineToRelative(2.68f, 2.68f)
                curveTo(3.06f, 7.83f, 1.77f, 9.53f, 1.0f, 11.5f)
                curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
                curveToRelative(1.52f, 0.0f, 2.98f, -0.29f, 4.32f, -0.82f)
                lineToRelative(3.42f, 3.42f)
                lineToRelative(1.41f, -1.41f)
                lineTo(3.42f, 2.45f)
                lineTo(2.01f, 3.87f)
                close()
                moveTo(9.51f, 11.37f)
                lineToRelative(2.61f, 2.61f)
                curveToRelative(-0.04f, 0.01f, -0.08f, 0.02f, -0.12f, 0.02f)
                curveToRelative(-1.38f, 0.0f, -2.5f, -1.12f, -2.5f, -2.5f)
                curveToRelative(0.0f, -0.05f, 0.01f, -0.08f, 0.01f, -0.13f)
                close()
                moveTo(6.11f, 7.97f)
                lineToRelative(1.75f, 1.75f)
                curveToRelative(-0.23f, 0.55f, -0.36f, 1.15f, -0.36f, 1.78f)
                curveToRelative(0.0f, 2.48f, 2.02f, 4.5f, 4.5f, 4.5f)
                curveToRelative(0.63f, 0.0f, 1.23f, -0.13f, 1.77f, -0.36f)
                lineToRelative(0.98f, 0.98f)
                curveToRelative(-0.88f, 0.24f, -1.8f, 0.38f, -2.75f, 0.38f)
                curveToRelative(-3.79f, 0.0f, -7.17f, -2.13f, -8.82f, -5.5f)
                curveToRelative(0.7f, -1.43f, 1.72f, -2.61f, 2.93f, -3.53f)
                close()
            }
        }
        return _visibilityOff!!
    }

private var _visibilityOff: ImageVector? = null

public val Icons.Outlined.Dashboard: ImageVector
    get() {
        if (_dashboard != null) {
            return _dashboard!!
        }
        _dashboard = materialIcon(name = "Outlined.Dashboard") {
            materialPath {
                moveTo(19.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(15.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                moveTo(9.0f, 5.0f)
                verticalLineToRelative(6.0f)
                lineTo(5.0f, 11.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                moveToRelative(10.0f, 8.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(4.0f)
                moveTo(9.0f, 17.0f)
                verticalLineToRelative(2.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                moveTo(21.0f, 3.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(8.0f)
                lineTo(21.0f, 3.0f)
                close()
                moveTo(11.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                verticalLineToRelative(10.0f)
                horizontalLineToRelative(8.0f)
                lineTo(11.0f, 3.0f)
                close()
                moveTo(21.0f, 11.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(10.0f)
                horizontalLineToRelative(8.0f)
                lineTo(21.0f, 11.0f)
                close()
                moveTo(11.0f, 15.0f)
                lineTo(3.0f, 15.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-6.0f)
                close()
            }
        }
        return _dashboard!!
    }

private var _dashboard: ImageVector? = null

public val Icons.Outlined.ManageAccounts: ImageVector
    get() {
        if (_manageAccounts != null) {
            return _manageAccounts!!
        }
        _manageAccounts = materialIcon(name = "Outlined.ManageAccounts") {
            materialPath {
                moveTo(4.0f, 18.0f)
                verticalLineToRelative(-0.65f)
                curveToRelative(0.0f, -0.34f, 0.16f, -0.66f, 0.41f, -0.81f)
                curveTo(6.1f, 15.53f, 8.03f, 15.0f, 10.0f, 15.0f)
                curveToRelative(0.03f, 0.0f, 0.05f, 0.0f, 0.08f, 0.01f)
                curveToRelative(0.1f, -0.7f, 0.3f, -1.37f, 0.59f, -1.98f)
                curveTo(10.45f, 13.01f, 10.23f, 13.0f, 10.0f, 13.0f)
                curveToRelative(-2.42f, 0.0f, -4.68f, 0.67f, -6.61f, 1.82f)
                curveTo(2.51f, 15.34f, 2.0f, 16.32f, 2.0f, 17.35f)
                verticalLineTo(20.0f)
                horizontalLineToRelative(9.26f)
                curveToRelative(-0.42f, -0.6f, -0.75f, -1.28f, -0.97f, -2.0f)
                horizontalLineTo(4.0f)
                close()
            }
            materialPath {
                moveTo(10.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                curveTo(7.79f, 4.0f, 6.0f, 5.79f, 6.0f, 8.0f)
                reflectiveCurveTo(7.79f, 12.0f, 10.0f, 12.0f)
                close()
                moveTo(10.0f, 6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveTo(8.9f, 6.0f, 10.0f, 6.0f)
                close()
            }
            materialPath {
                moveTo(20.75f, 16.0f)
                curveToRelative(0.0f, -0.22f, -0.03f, -0.42f, -0.06f, -0.63f)
                lineToRelative(1.14f, -1.01f)
                lineToRelative(-1.0f, -1.73f)
                lineToRelative(-1.45f, 0.49f)
                curveToRelative(-0.32f, -0.27f, -0.68f, -0.48f, -1.08f, -0.63f)
                lineTo(18.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                lineToRelative(-0.3f, 1.49f)
                curveToRelative(-0.4f, 0.15f, -0.76f, 0.36f, -1.08f, 0.63f)
                lineToRelative(-1.45f, -0.49f)
                lineToRelative(-1.0f, 1.73f)
                lineToRelative(1.14f, 1.01f)
                curveToRelative(-0.03f, 0.21f, -0.06f, 0.41f, -0.06f, 0.63f)
                reflectiveCurveToRelative(0.03f, 0.42f, 0.06f, 0.63f)
                lineToRelative(-1.14f, 1.01f)
                lineToRelative(1.0f, 1.73f)
                lineToRelative(1.45f, -0.49f)
                curveToRelative(0.32f, 0.27f, 0.68f, 0.48f, 1.08f, 0.63f)
                lineTo(16.0f, 21.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(0.3f, -1.49f)
                curveToRelative(0.4f, -0.15f, 0.76f, -0.36f, 1.08f, -0.63f)
                lineToRelative(1.45f, 0.49f)
                lineToRelative(1.0f, -1.73f)
                lineToRelative(-1.14f, -1.01f)
                curveTo(20.72f, 16.42f, 20.75f, 16.22f, 20.75f, 16.0f)
                close()
                moveTo(17.0f, 18.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveTo(18.1f, 18.0f, 17.0f, 18.0f)
                close()
            }
        }
        return _manageAccounts!!
    }

private var _manageAccounts: ImageVector? = null

public val Icons.Outlined.Restore: ImageVector
    get() {
        if (_restore != null) {
            return _restore!!
        }
        _restore = materialIcon(name = "Outlined.Restore") {
            materialPath {
                moveTo(13.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                lineTo(1.0f, 12.0f)
                lineToRelative(4.0f, 3.99f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.25f, 2.52f)
                lineToRelative(0.77f, -1.28f)
                lineToRelative(-3.52f, -2.09f)
                lineTo(13.5f, 8.0f)
                close()
            }
        }
        return _restore!!
    }

private var _restore: ImageVector? = null

public val Icons.Outlined.TextFields: ImageVector
    get() {
        if (_textFields != null) {
            return _textFields!!
        }
        _textFields = materialIcon(name = "Outlined.TextFields") {
            materialPath {
                moveTo(2.5f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(3.0f)
                lineTo(10.5f, 7.0f)
                horizontalLineToRelative(5.0f)
                lineTo(15.5f, 4.0f)
                horizontalLineToRelative(-13.0f)
                close()
                moveTo(21.5f, 9.0f)
                horizontalLineToRelative(-9.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(3.0f)
                lineTo(21.5f, 9.0f)
                close()
            }
        }
        return _textFields!!
    }

private var _textFields: ImageVector? = null

public val Icons.Outlined.Update: ImageVector
    get() {
        if (_update != null) {
            return _update!!
        }
        _update = materialIcon(name = "Outlined.Update") {
            materialPath {
                moveTo(11.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.25f, 2.52f)
                lineToRelative(0.77f, -1.28f)
                lineToRelative(-3.52f, -2.09f)
                verticalLineTo(8.0f)
                horizontalLineTo(11.0f)
                close()
                moveTo(21.0f, 10.0f)
                verticalLineTo(3.0f)
                lineToRelative(-2.64f, 2.64f)
                curveTo(16.74f, 4.01f, 14.49f, 3.0f, 12.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                reflectiveCurveToRelative(4.03f, 9.0f, 9.0f, 9.0f)
                reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(0.0f, 3.86f, -3.14f, 7.0f, -7.0f, 7.0f)
                reflectiveCurveToRelative(-7.0f, -3.14f, -7.0f, -7.0f)
                reflectiveCurveToRelative(3.14f, -7.0f, 7.0f, -7.0f)
                curveToRelative(1.93f, 0.0f, 3.68f, 0.79f, 4.95f, 2.05f)
                lineTo(14.0f, 10.0f)
                horizontalLineTo(21.0f)
                close()
            }
        }
        return _update!!
    }

private var _update: ImageVector? = null

public val Icons.Outlined.MeetingRoom: ImageVector
    get() {
        if (_meetingRoom != null) {
            return _meetingRoom!!
        }
        _meetingRoom = materialIcon(name = "Outlined.MeetingRoom") {
            materialPath {
                moveTo(19.0f, 19.0f)
                lineTo(19.0f, 4.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(15.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                verticalLineToRelative(16.0f)
                lineTo(3.0f, 19.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                lineTo(15.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(15.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(13.0f, 19.0f)
                lineTo(7.0f, 19.0f)
                lineTo(7.0f, 5.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(14.0f)
                close()
                moveTo(10.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                close()
            }
        }
        return _meetingRoom!!
    }

private var _meetingRoom: ImageVector? = null

public val Icons.AutoMirrored.Outlined.Assignment: ImageVector
    get() {
        if (_assignment != null) {
            return _assignment!!
        }
        _assignment = materialIcon(name = "AutoMirrored.Outlined.Assignment", autoMirror = true) {
            materialPath {
                moveTo(7.0f, 15.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 17.0f)
                close()
                moveTo(7.0f, 11.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 13.0f)
                close()
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 9.0f)
                close()
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 1.84f, 13.3f, 1.0f, 12.0f, 1.0f)
                curveToRelative(-1.3f, 0.0f, -2.4f, 0.84f, -2.82f, 2.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-0.14f, 0.0f, -0.27f, 0.01f, -0.4f, 0.04f)
                curveToRelative(-0.39f, 0.08f, -0.74f, 0.28f, -1.01f, 0.55f)
                curveToRelative(-0.18f, 0.18f, -0.33f, 0.4f, -0.43f, 0.64f)
                curveToRelative(-0.1f, 0.23f, -0.16f, 0.49f, -0.16f, 0.77f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 0.27f, 0.06f, 0.54f, 0.16f, 0.78f)
                reflectiveCurveToRelative(0.25f, 0.45f, 0.43f, 0.64f)
                curveToRelative(0.27f, 0.27f, 0.62f, 0.47f, 1.01f, 0.55f)
                curveToRelative(0.13f, 0.02f, 0.26f, 0.03f, 0.4f, 0.03f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 2.75f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                reflectiveCurveToRelative(-0.34f, 0.75f, -0.75f, 0.75f)
                reflectiveCurveToRelative(-0.75f, -0.34f, -0.75f, -0.75f)
                reflectiveCurveToRelative(0.34f, -0.75f, 0.75f, -0.75f)
                close()
                moveTo(19.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(14.0f)
                close()
            }
        }
        return _assignment!!
    }

private var _assignment: ImageVector? = null

public val Icons.AutoMirrored.Outlined.FactCheck: ImageVector
    get() {
        if (_factCheck != null) {
            return _factCheck!!
        }
        _factCheck = materialIcon(name = "AutoMirrored.Outlined.FactCheck", autoMirror = true) {
            materialPath(pathFillType = EvenOdd) {
                moveTo(20.0f, 3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 3.0f, 2.0f, 3.9f, 2.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(22.0f, 3.9f, 21.1f, 3.0f, 20.0f, 3.0f)
                close()
                moveTo(20.0f, 19.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(19.0f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(19.41f, 10.42f)
                lineToRelative(-1.42f, -1.42f)
                lineToRelative(-3.17f, 3.17f)
                lineToRelative(-1.41f, -1.42f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(2.82f, 2.84f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(5.0f, 7.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-5.0f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(5.0f, 11.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-5.0f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(5.0f, 15.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-5.0f)
                close()
            }
        }
        return _factCheck!!
    }

private var _factCheck: ImageVector? = null

public val Icons.AutoMirrored.Outlined.Logout: ImageVector
    get() {
        if (_logout != null) {
            return _logout!!
        }
        _logout = materialIcon(name = "AutoMirrored.Outlined.Logout", autoMirror = true) {
            materialPath {
                moveTo(17.0f, 8.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(17.17f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.17f)
                lineToRelative(-1.58f, 1.58f)
                lineTo(17.0f, 16.0f)
                lineToRelative(4.0f, -4.0f)
                lineTo(17.0f, 8.0f)
                close()
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                close()
            }
        }
        return _logout!!
    }

private var _logout: ImageVector? = null

public val Icons.Outlined.Tv: ImageVector
    get() {
        if (_tv != null) {
            return _tv!!
        }
        _tv = materialIcon(name = "Outlined.Tv") {
            materialPath {
                moveTo(21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(1.1f, 0.0f, 1.99f, -0.9f, 1.99f, -2.0f)
                lineTo(23.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(21.0f, 17.0f)
                lineTo(3.0f, 17.0f)
                lineTo(3.0f, 5.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(12.0f)
                close()
            }
        }
        return _tv!!
    }

private var _tv: ImageVector? = null
