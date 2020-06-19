module Main exposing (Model(..), Msg(..), Room, getRooms, init, main, renderList, roomsDecoder, subscriptions, update, view, viewRooms)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (Decoder, field, string)
import Json.Encode exposing (Value, int, object)



-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type alias Room =
    { room_id : Int
    , name : String
    , sticky : Bool
    , icon_path : String
    , last_update_time : Int
    , isChecked : Bool
    }


type Model
    = Failure
    | Loading
    | Data (List Room)


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getRooms )



-- UPDATE


type Msg
    = MorePlease
    | GetRooms (Result Http.Error (List Room))
    | Checked Room
    | AlartChat (Result Http.Error String)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        MorePlease ->
            ( Loading, getRooms )

        GetRooms result ->
            case result of
                Ok rooms ->
                    ( Data rooms, Cmd.none )

                error ->
                    Debug.log (Debug.toString error)
                        ( Failure, Cmd.none )

        Checked newRoom ->
            case model of
                Data rooms ->
                    let
                        newRooms =
                            rooms
                                |> List.map (\room -> { room | isChecked = reverseCheck room.room_id newRoom.room_id room.isChecked })
                    in
                    ( Data newRooms, alarmChat newRoom.room_id (not newRoom.isChecked) )

                _ ->
                    ( Loading, getRooms )

        AlartChat result ->
            case result of
                Ok _ ->
                    ( model, Cmd.none )

                error ->
                    Debug.log (Debug.toString error)
                        ( Failure, Cmd.none )


reverseCheck : Int -> Int -> Bool -> Bool
reverseCheck roomId checkedRoomId isChecked =
    if roomId == checkedRoomId then
        not isChecked

    else
        isChecked



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ h2 [] [ text "Random Cats" ]
        , viewRooms model
        ]


viewRooms : Model -> Html Msg
viewRooms model =
    case model of
        Failure ->
            div []
                [ text "失敗"
                , button [ onClick MorePlease ] [ text "Try Again!" ]
                ]

        Loading ->
            text "Loading..."

        Data rooms ->
            div []
                [ renderList rooms ]


renderList : List Room -> Html Msg
renderList lst =
    ul []
        (List.map
            (\room ->
                li []
                    [ p []
                        [ div [ class "columns" ]
                            [ div [ class "column is-1 toggle_width" ]
                                [ div [ class "switch" ]
                                    [ label [ class "switch__label" ]
                                        [ checkbox room
                                        , span [ class "switch__content" ] []
                                        , span [ class "switch__circle" ] []
                                        ]
                                    ]
                                ]
                            , div [ class "column" ]
                                [ img [ class "icon_size icon_position", src room.icon_path ] []
                                , text room.name
                                ]
                            ]
                        ]
                    ]
            )
            lst
        )


checkbox : Room -> Html Msg
checkbox room =
    input
        [ type_ "checkbox"
        , class "switch__input"
        , checked room.isChecked
        , onClick (Checked room)
        ]
        []



-- Cmd


getRooms : Cmd Msg
getRooms =
    Http.request
        { method = "GET"
        , headers =
            [ Http.header "Accept" "application/json"
            , Http.header "Content-Type" "application/json"
            ]
        , url = "/targets"
        , expect = Http.expectJson GetRooms roomsDecoder
        , body = Http.emptyBody
        , timeout = Just 10000
        , tracker = Nothing
        }


alarmChat : Int -> Bool -> Cmd Msg
alarmChat room_id isChecked =
    Http.request
        { method = "POST"
        , headers =
            [ Http.header "Accept" "application/json"
            , Http.header "Content-Type" "application/json"
            ]
        , url = "/alarm"
        , expect = Http.expectJson AlartChat alartDecoder
        , body = Http.jsonBody <| Json.Encode.object [ ( "room_id", Json.Encode.int room_id ), ( "isChecked", Json.Encode.bool isChecked ) ]
        , timeout = Just 10000
        , tracker = Nothing
        }



-- DECODER


roomDecoder : Decoder Room
roomDecoder =
    Json.Decode.map6 Room
        (Json.Decode.field "room_id" Json.Decode.int)
        (Json.Decode.field "name" Json.Decode.string)
        (Json.Decode.field "sticky" Json.Decode.bool)
        (Json.Decode.field "icon_path" Json.Decode.string)
        (Json.Decode.field "last_update_time" Json.Decode.int)
        (Json.Decode.field "isChecked" Json.Decode.bool)


roomsDecoder : Decoder (List Room)
roomsDecoder =
    Json.Decode.list roomDecoder


alartDecoder : Decoder String
alartDecoder =
    Json.Decode.field "result" Json.Decode.string
