package com.arellomobile.mvp.sample.github.mvp.presenters;

import android.content.Context;

import com.arellomobile.mvp.sample.github.di.AppComponent;
import com.arellomobile.mvp.sample.github.mvp.GithubService;
import com.arellomobile.mvp.sample.github.mvp.models.Repository;
import com.arellomobile.mvp.sample.github.mvp.views.RepositoriesView$$State;
import com.arellomobile.mvp.sample.github.test.GithubSampleTestRunner;
import com.arellomobile.mvp.sample.github.test.TestComponent;
import com.arellomobile.mvp.sample.github.test.TestComponentRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GithubSampleTestRunner.class)
public class RepositoriesPresenterTest {

    @Rule
    public TestComponentRule testComponentRule = new TestComponentRule(testAppComponent());

    @Mock
    GithubService githubService;

    @Mock
    RepositoriesView$$State repositoriesViewState;

    private RepositoriesPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new RepositoriesPresenter();
        presenter.setViewState(repositoriesViewState);
    }

    @Test
    public void repositories_shouldCloseError() {
        presenter.closeError();
        verify(repositoriesViewState).hideError();
    }


    @Test
    public void repositories_shouldOnAttachLoadAndShowRepositores() {
        List<Repository> repositories = repositores();
        when(githubService.getUserRepos(anyString(), anyInt(), anyInt())).thenReturn(Observable.just(repositories));

        presenter.onFirstViewAttach();
        verify(repositoriesViewState).hideError();
        verify(repositoriesViewState).onStartLoading();
        verify(repositoriesViewState, never()).showRefreshing();
        verify(repositoriesViewState).showListProgress();
        verify(repositoriesViewState).onFinishLoading();
        verify(repositoriesViewState).setRepositories(repositories, false);
    }

    @Test
    public void repositories_shouldCorrectLoadNextRepositories() {
        List<Repository> repositories = repositores();
        when(githubService.getUserRepos(anyString(), anyInt(), anyInt())).thenReturn(Observable.just(repositories));

        presenter.loadNextRepositories(10);
        verify(repositoriesViewState).hideError();
        verify(repositoriesViewState).onStartLoading();
        verify(repositoriesViewState, never()).showListProgress();
        verify(repositoriesViewState, never()).hideListProgress();
        verify(repositoriesViewState).onFinishLoading();
        verify(repositoriesViewState).addRepositories(repositories, false);
    }

    @Test
    public void repositories_shouldShowErrorIfSomeExceptionHappended() {
        RuntimeException someError = new RuntimeException();
        when(githubService.getUserRepos(anyString(), anyInt(), anyInt())).thenReturn(Observable.error(someError));

        presenter.loadNextRepositories(10);
        verify(repositoriesViewState).hideError();
        verify(repositoriesViewState).onStartLoading();
        verify(repositoriesViewState).onFinishLoading();
        verify(repositoriesViewState).showError(someError.toString());
    }


    private List<Repository> repositores() {
        List<Repository> repositories = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            repositories.add(new Repository());
        }
        return repositories;
    }

    private AppComponent testAppComponent() {
        return new TestComponent() {
            @Override public void inject(RepositoriesPresenter presenter) {
                presenter.mGithubService = githubService;
            }
        };
    }

}