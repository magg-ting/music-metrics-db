package com.example.melody_meter_local.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.melody_meter_local.R
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.rating.RatingDialog
import com.maxkeppeler.sheets.rating.models.FeedbackTextFieldType
import com.maxkeppeler.sheets.rating.models.RatingBody
import com.maxkeppeler.sheets.rating.models.RatingConfig
import com.maxkeppeler.sheets.rating.models.RatingSelection
import com.maxkeppeler.sheets.rating.models.RatingViewStyle

/**
 * https://github.com/maxkeppeler/sheets-compose-dialogs/blob/main/rating/src/main/java/com/maxkeppeler/sheets/rating/RatingDialog.kt
 * Rating dialog use-case.
 * @param state The state of the sheet.
 * @param selection The selection configuration for the dialog.
 * @param config The configuration for the rating view.
 * @param header The header to be displayed at the top of the dialog.
 * @param body The body content to be displayed inside the dialog.
 * @param properties DialogProperties for further customization of this dialog's behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingDialogComponent (closeSelection: () -> Unit, onRatingSelected: (Double) -> Unit) {
    RatingDialog(
        state = rememberUseCaseState(
            visible = true,
            onCloseRequest = { closeSelection() }),
//        header = Header.Default(
//            title = "Rate Us",
//            icon = IconSource(Icons.Outlined.Star)
//        ),
        body = RatingBody.Default(
            bodyText = stringResource(R.string.rating_prompt)
        ),
        config = RatingConfig(
            ratingViewStyle = RatingViewStyle.CENTER,
            ratingOptionsCount = 5,
            withFeedback = false,
//            feedbackOptional = true,
//            feedbackTextFieldType = FeedbackTextFieldType.OUTLINED,
        ),
        selection = RatingSelection(
            onSelectRating = { rating, feedback ->
                onRatingSelected(rating.toDouble())
            },
        )
    )
}
