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
    { roomId : Int
    , name : String
    , sticky : Bool
    , iconPath : String
    , lastUpdateTime : Int
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
    = OneMorePlease
    | GetRooms (Result Http.Error (List Room))
    | Checked Room
    | AlartChat (Result Http.Error String)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        OneMorePlease ->
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
                                |> List.map (\room -> { room | isChecked = reverseCheck room.roomId newRoom.roomId room.isChecked })
                    in
                    ( Data newRooms, alartChat newRoom.roomId newRoom.name (not newRoom.isChecked) )

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
        [ h2 [] [ text "ピン止めしたルームの一覧" ]
        , viewRooms model
        ]


viewRooms : Model -> Html Msg
viewRooms model =
    case model of
        Failure ->
            div []
                [ text "失敗"
                , button [ onClick OneMorePlease ] [ text "Try Again!" ]
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
                                [ img [ class "icon_size icon_position", src room.iconPath ] []
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


alartChat : Int -> String -> Bool -> Cmd Msg
alartChat roomId chatName isChecked =
    Http.request
        { method = "POST"
        , headers =
            [ Http.header "Accept" "application/json"
            ]
        , url = "/alartswitch"
        , expect = Http.expectJson AlartChat alartDecoder
        , body = Http.jsonBody <| Json.Encode.object [ ( "roomId", Json.Encode.int roomId ), ( "chatName", Json.Encode.string chatName ), ( "isChecked", Json.Encode.bool isChecked ) ]
        , timeout = Just 10000
        , tracker = Nothing
        }



-- DECODER


roomDecoder : Decoder Room
roomDecoder =
    Json.Decode.map6 Room
        (Json.Decode.field "roomId" Json.Decode.int)
        (Json.Decode.field "name" Json.Decode.string)
        (Json.Decode.field "sticky" Json.Decode.bool)
        (Json.Decode.field "iconPath" Json.Decode.string)
        (Json.Decode.field "lastUpdateTime" Json.Decode.int)
        (Json.Decode.field "isChecked" Json.Decode.bool)


roomsDecoder : Decoder (List Room)
roomsDecoder =
    Json.Decode.list roomDecoder


alartDecoder : Decoder String
alartDecoder =
    Json.Decode.field "result" Json.Decode.string
