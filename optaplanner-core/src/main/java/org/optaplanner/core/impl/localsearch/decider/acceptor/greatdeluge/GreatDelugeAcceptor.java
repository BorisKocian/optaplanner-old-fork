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
    private double[] initialLevelScoreLevels;
    private double[] levelScoreLevels;

    private double levelMinimum = 0;
    private final double THRESHOLD = .0001;

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
        initialLevelScoreLevels = ScoreUtils.extractLevelDoubles(initialLevel);
        levelScoreLevels = initialLevelScoreLevels;
        levelsLength = levelScoreLevels.length;
    }

    public void phaseEnded(LocalSearchPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        initialLevelScoreLevels = null;
        levelScoreLevels = null;
        levelsLength = -1;
    }

    @Override
    public boolean isAccepted(LocalSearchMoveScope moveScope) {

        Score moveScore = moveScope.getScore();

        double[] moveScoreLevels = ScoreUtils.extractLevelDoubles(moveScore);

        for (int i = 0; i < levelsLength; i++) {

            double moveScoreLevel = moveScoreLevels[i];
            double levelScoreLevel = -levelScoreLevels[i];

            if (moveScoreLevel > levelScoreLevel) {
                return true;
            } else if (Math.abs(moveScoreLevel - levelScoreLevel) < THRESHOLD) {
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
            levelScoreLevels[i] = initialLevelScoreLevels[i] - rainSpeed;
            if (levelScoreLevels[i] < levelMinimum) {
                levelScoreLevels[i] = levelMinimum;
            }
        }
    }
}
