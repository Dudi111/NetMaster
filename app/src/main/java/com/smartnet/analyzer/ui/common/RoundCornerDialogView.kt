package com.smartnet.analyzer.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartnet.analyzer.common.theme.colorAccent

@Composable
fun RoundCornerDialogView(
    message: Int,
    btnOKResID: Int,
    btnCancelResID: Int,
    openDialogCustom: MutableState<Boolean>,
    isCancel: MutableState<Boolean>,
    onOkClick: () -> Unit,
    onCancelClick: () -> Unit = onOkClick
) {
    Dialog(
        onDismissRequest = { !openDialogCustom.value },
        content = {
            val systemUiController = rememberSystemUiController()
            systemUiController.isNavigationBarVisible = false // Navigation bar
            systemUiController.navigationBarDarkContentEnabled =false
            CustomDialogUI(message, btnOKResID, btnCancelResID, isCancel, onOkClick, onCancelClick)
        }, properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
    )
}

@Composable
fun CustomDialogUI(
    message: Int,
    btnOKResID: Int,
    btnCancelResID: Int,
    isCancel: MutableState<Boolean>,
    onOkClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Card(
        elevation = 10.dp,
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(color = colorAccent)
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(message),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(25.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onOkClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Gray
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        //.semantics { this.contentDescription = btnDialogPositive; this.testTag = btnDialogPositive },
                ) {
                    Text(
                        text = stringResource(btnOKResID).uppercase(),
                        fontSize =  15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
                if (isCancel.value) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Button(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White,
                            contentColor = Color.Gray
                        ),
                        modifier = Modifier
                            .width(110.dp)
                         //   .semantics { this.contentDescription = btnDialogNegative; this.testTag = btnDialogNegative },
                    ) {
                        Text(
                            text = stringResource(btnCancelResID).uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}