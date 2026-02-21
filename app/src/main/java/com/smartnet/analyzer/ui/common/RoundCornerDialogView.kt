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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartnet.analyzer.common.dimen_10dp
import com.smartnet.analyzer.common.dimen_14sp
import com.smartnet.analyzer.common.dimen_15dp
import com.smartnet.analyzer.common.dimen_18sp
import com.smartnet.analyzer.common.dimen_35dp
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
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimen_10dp)
    ) {
        Column(
            modifier = Modifier
                .background(color = colorAccent)
                .padding(dimen_15dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(message),
                fontSize = dimen_18sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(dimen_35dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onOkClick,
                    shape = RoundedCornerShape(dimen_10dp),
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Gray,
                        disabledContainerColor = Color.White,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier
                        .wrapContentWidth()
                        //.semantics { this.contentDescription = btnDialogPositive; this.testTag = btnDialogPositive },
                ) {
                    Text(
                        text = stringResource(btnOKResID).uppercase(),
                        fontSize =  dimen_14sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
                if (isCancel.value) {
                    Spacer(modifier = Modifier.width(dimen_15dp))
                    Button(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(dimen_10dp),
                        colors = ButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Gray,
                            disabledContainerColor = Color.White,
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier
                            .wrapContentWidth()
                         //   .semantics { this.contentDescription = btnDialogNegative; this.testTag = btnDialogNegative },
                    ) {
                        Text(
                            text = stringResource(btnCancelResID).uppercase(),
                            fontSize = dimen_14sp,
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