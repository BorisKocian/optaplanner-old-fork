package org.optaplanner.core.impl.localsearch.decider.acceptor.greatdeluge;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.localsearch.decider.acceptor.AbstractAcceptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.score.ScoreUtils;

public class GreatDelugeAcceptor extends AbstractAcceptor {

    private Score initialLevel;
    private double rainSpeed;

    private int levelsLength = -1;
    private double[] initialLevelLevels;
    private double[] levelLevels;

    private double levelMinimum = 0;

    public void setInitialLevels(Score initialLevel) { this.initialLevel = initialLevel; }

    public void setRainSpeed(double rainSpeed) { this.rainSpeed = rainSpeed; }

    public void phaseStarted(LocalSearchPhaseScope phaseScope) {
        super.phaseStarted(phaseScope);
        for (double initialLevelLevel : ScoreUtils.extractLevelDoubles(initialLevel)) {
            if (initialLevelLevel < 0.0) {
                throw new IllegalArgumentException("The initial level (" + initialLevel
                        + ") cannot have negative level (" + initialLevelLevel + ").");
            }
        }
        initialLevelLevels = ScoreUtils.extractLevelDoubles(initialLevel);
        levelLevels = initialLevelLevels;
        levelsLength = levelLevels.length;
    }

    public void phaseEnded(LocalSearchPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        initialLevelLevels = null;
        levelLevels = null;
        levelsLength = -1;
    }

    @Override
    public boolean isAccepted(LocalSearchMoveScope moveScope) {

        LocalSearchPhaseScope phaseScope = moveScope.getStepScope().getPhaseScope();
        Score lastStepScore = phaseScope.getLastCompletedStepScope().getScore();
        Score moveScore = moveScope.getScore();

        /*
        if (moveScore.compareTo(lastStepScore) >= 0) {
            return true;
        }
        */

        double[] moveScoreLevels = ScoreUtils.extractLevelDoubles(moveScore);

        for (int i = 0; i < levelsLength; i++) {

            double moveScoreLevel = moveScoreLevels[i];
            double levelLevel = levelLevels[i];

            if (moveScoreLevel > -levelLevel) {
                return true;
            } else if (moveScoreLevel == -levelLevel) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    // change water level at the beginning of the step
    public void stepStarted(LocalSearchStepScope stepScope) {
        super.stepEnded(stepScope);
        for (int i = 0; i < levelsLength; i++) {
            levelLevels[i] = initialLevelLevels[i] - rainSpeed;
            if (levelLevels[i] < levelMinimum) {
                levelLevels[i] = levelMinimum;
            }

        }

    }
}
